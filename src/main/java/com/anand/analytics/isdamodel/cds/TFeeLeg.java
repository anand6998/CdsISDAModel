package com.anand.analytics.isdamodel.cds;


import com.anand.analytics.isdamodel.utils.*;
import org.apache.log4j.Logger;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.*;

import static com.anand.analytics.isdamodel.cds.TDateFunctions.adjustedBusinessDay;
import static com.anand.analytics.isdamodel.cds.TDateFunctions.cdsDayCountFraction;
import static com.anand.analytics.isdamodel.cds.TDateFunctions.dtFwdAny;
import static com.anand.analytics.isdamodel.cds.TRateFunctions.cdsForwardZeroPrice;
import static com.anand.analytics.isdamodel.cds.TRateFunctions.cdsZeroPrice;

//TODO - move this class
class TMonthDayYear {
    int year, month, day;
    public boolean isLeap;
}

/**
 * Created by aanand on 10/21/2014.
 */
public class TFeeLeg {

    static Logger logger = Logger.getLogger(TFeeLeg.class);

    final int nbDates;
    /**
     * Array of size nbDates.
     * Start date for calculating accrued interest.
     */
    final LocalDate[] accStartDates;
    /**
     * Array of size nbDates.
     * End date for calculating accrued interest.
     */
    final LocalDate[] accEndDates;
    /**
     * Array of size nbDates.
     * Payment date for each fee payment.
     */
    final LocalDate[] payDates;
    /**
     * Notional
     */
    final double notional;
    /**
     * Coupon rate of fee payment.
     */
    final double couponRate;
    /**
     * Day count convention for computing fee payments and accruals in
     * case of default.
     */
    final DayCount dcc;
    /**
     * Determines how accruals are handled in case of default.
     */
    final TAccrualPayConv accrualPayConv;
    /**
     * Denotes whether observation of defaults is at the start of the day
     * or the end of the day for the accrual start and end dates.
     */
    final boolean obsStartOfDay;

    public TFeeLeg(
            LocalDate startDate,
            LocalDate endDate,
            boolean payAccruedOnDefault,
            TDateInterval dateInterval,
            TStubMethod stubType,
            double notional,
            double couponRate,
            DayCount paymentDcc,
            TBadDayConvention badDayConv,
            String calendar,
            boolean protectStart) {
        TDateInterval ivl3M = new TDateInterval(3, PeriodType.M, 0);
        TDateList dl;

        if (dateInterval == null)
            dateInterval = ivl3M;

        if (protectStart && endDate.isEqual(startDate)) {
            LocalDate[] dates = new LocalDate[2];
            dates[0] = startDate;
            dates[1] = endDate;
            dl = new TDateList();
            dl.dateArray = dates;
            dl.fNumItems = 2;
        } else {
            dl = dateListMakeRegular(startDate, endDate, dateInterval, stubType);
        }

        /* the datelist includes both start date and end date */
        /* therefore it has one more element than the fee leg requires */
        int numItems = dl.fNumItems - 1;
        this.accStartDates = new LocalDate[numItems];
        this.accEndDates = new LocalDate[numItems];
        this.payDates = new LocalDate[numItems];
        this.nbDates = numItems;

        this.accrualPayConv = payAccruedOnDefault ? TAccrualPayConv.ACCRUAL_PAY_ALL
                : TAccrualPayConv.ACCRUAL_PAY_NONE;
        this.dcc = paymentDcc;

        LocalDate prevDate = dl.dateArray[0];
        LocalDate prevDateAdj = prevDate; /*first date is not bad day adjusted*/

        for (int i = 0; i < nbDates; i++) {
            LocalDate nextDate = dl.dateArray[i + 1];
            LocalDate nextDateAdj = adjustedBusinessDay(nextDate, badDayConv, calendar);
            this.accStartDates[i] = prevDateAdj;
            this.accEndDates[i] = nextDateAdj;
            this.payDates[i] = nextDateAdj;

            prevDate = nextDate;
            prevDateAdj = nextDateAdj;
        }

        this.notional = notional;
        this.couponRate = couponRate;

        /* the last accrual date is not adjusted */
        /* also we may have one extra day of accrued interest */
        if (protectStart) {
            this.accEndDates[this.nbDates - 1] = prevDate.plusDays(1);
            this.obsStartOfDay = true;
        } else {
            this.accEndDates[this.nbDates - 1] = prevDate;
            this.obsStartOfDay = false;
        }
    }

    public int getNbDates() {
        return nbDates;
    }

    public LocalDate[] getAccStartDates() {
        return accStartDates;
    }

    public LocalDate[] getAccEndDates() {
        return accEndDates;
    }

    public LocalDate[] getPayDates() {
        return payDates;
    }

    public double getNotional() {
        return notional;
    }

    public double getCouponRate() {
        return couponRate;
    }

    public DayCount getDcc() {
        return dcc;
    }

    public TAccrualPayConv getAccrualPayConv() {
        return accrualPayConv;
    }

    public boolean isObsStartOfDay() {
        return obsStartOfDay;
    }

    public ReturnStatus getPV(LocalDate today,
                              LocalDate valueDate,
                              LocalDate stepinDate,
                              TCurve discountCurve,
                              TCurve spreadCurve,
                              boolean payAccruedAtStart,
                              DoubleHolder result
    ) {
        double myPv = 0.0;
        double valueDatePv;

        LocalDate matDate;
        TDateList tl = null;

        assert (discountCurve != null);
        assert (spreadCurve != null);
        assert (result != null);

        assert (valueDate.isAfter(today));
        assert (stepinDate.isAfter(today) || stepinDate.isAfter(today));

        double CDS_LOG0_THRESHOLD = 1e-100;
        if (nbDates > 1) {
            /**
             * It is more efficient to compute the timeline just once and
             * truncate is for each payment
             *
             */
            LocalDate startDate = accStartDates[0];
            LocalDate endDate = accEndDates[nbDates - 1];

            double rate = cdsZeroPrice(spreadCurve, endDate);
            assert (rate > CDS_LOG0_THRESHOLD);

            rate = cdsZeroPrice(discountCurve, endDate);
            assert (rate > CDS_LOG0_THRESHOLD);

            tl = cdsRiskyTimeLine(startDate, endDate, discountCurve, spreadCurve);

        }

        matDate = obsStartOfDay == true ? accEndDates[nbDates - 1].plusDays(-1) : accEndDates[nbDates - 1];
        if (today.isAfter(matDate) || stepinDate.isAfter(matDate)) {
            result.set(0);
            return ReturnStatus.SUCCESS;
        }

        for (int i = 0; i < nbDates; ++i) {
            DoubleHolder thisPV = new DoubleHolder(0);
            feePaymentPVWithTimeline(accrualPayConv,
                    today,
                    stepinDate,
                    accStartDates[i],
                    accEndDates[i],
                    payDates[i],
                    dcc,
                    notional,
                    couponRate,
                    discountCurve,
                    spreadCurve,
                    tl,
                    obsStartOfDay,
                    thisPV);

            myPv += thisPV.get();
        }

        valueDatePv = cdsForwardZeroPrice(discountCurve, today, valueDate);
        result.set(myPv / valueDatePv);

        if (payAccruedAtStart) /* clean price */ {
            DoubleHolder ai = new DoubleHolder();
            feeLegAI(stepinDate, ai);
            double pv = result.get();
            pv -= ai.get();
            result.set(pv);
        }

        return ReturnStatus.SUCCESS;
    }

    private ReturnStatus feeLegAI(LocalDate today, DoubleHolder ai) {
        IntHolder exact = new IntHolder();
        IntHolder lo = new IntHolder();
        IntHolder hi = new IntHolder();

        if (today.isBefore(this.accStartDates[0])
                || today.isEqual(this.accStartDates[0])
                || today.isAfter(this.accEndDates[accEndDates.length - 1])
                || today.isEqual(this.accEndDates[accEndDates.length - 1])
                ) {
            ai.set(0);
            return ReturnStatus.SUCCESS;
        }

        if (CdsUtils.binarySearchLong(today,
                this.accStartDates,
                exact,
                lo,
                hi).equals(ReturnStatus.FAILURE)) {
            logger.error("Error in feeLegAI");
            return ReturnStatus.FAILURE;
        }

        if (exact.get() >= 0) {
            //today is on accrual start date
            ai.set(0);
        } else {
            //calculate ai, today is bracketed in some accrual period
            DoubleHolder accrual = new DoubleHolder();
            if (cdsDayCountFraction(this.accStartDates[lo.get()], today, this.dcc, accrual).equals(ReturnStatus.FAILURE)) {
                logger.error("Error in feeLegAI");
                return ReturnStatus.FAILURE;
            }

            double accrualValue = accrual.get();
            accrual.set(accrualValue * this.couponRate * this.notional);
            ai.set(accrual.get());
        }

        return ReturnStatus.SUCCESS;
    }


    public static double min(double a, double b) {
        return a < b ? a : b;
    }


    ReturnStatus feePaymentPVWithTimeline(TAccrualPayConv accrualPayConv,
                                          LocalDate today,
                                          LocalDate stepInDate,
                                          LocalDate accStartDate,
                                          LocalDate accEndDate,
                                          LocalDate payDate,
                                          DayCount accruedDCC,
                                          double notional,
                                          double couponRate,
                                          TCurve discountCurve,
                                          TCurve spreadCurve,
                                          TDateList tl,
                                          boolean obsStartOfDay,
                                          DoubleHolder pv) {
        double myPv = 0;

        /**
         * Because survival is calculated at the end of the day, then if
         * we observe survival at the start of the day, we need to subtract
         * one from the date
         */
        int obsOffset = obsStartOfDay ? -1 : 0;
        assert (discountCurve != null);
        assert (spreadCurve != null);
        assert (pv != null);

        if (accEndDate.isEqual(stepInDate) || accEndDate.isBefore(stepInDate)) {
            pv.set(0);
            return ReturnStatus.SUCCESS;
        }

        switch (accrualPayConv) {
            case ACCRUAL_PAY_NONE: {
                /**
                 * fee leg pays at pay date if it has survived to accrual end date
                 */
                double amount, survival, discount;
                DoubleHolder accrualTime = new DoubleHolder();

                cdsDayCountFraction(accStartDate, accEndDate, accruedDCC, accrualTime);
                amount = notional * couponRate * accrualTime.get();
                survival = cdsForwardZeroPrice(spreadCurve, today, accEndDate.plusDays(obsOffset));
                discount = cdsForwardZeroPrice(discountCurve, today, payDate);

                myPv = amount * survival * discount;
            }
            break;
            case ACCRUAL_PAY_ALL: {
                /**
                 * fee leg pays accrual on default - otherwise it pays at pay date
                 * if it has survived to accrual end date
                 */
                double amount, survival, discount;
                DoubleHolder accrual = new DoubleHolder(0);
                DoubleHolder accrualTime = new DoubleHolder();
                cdsDayCountFraction(accStartDate, accEndDate, accruedDCC, accrualTime);


                amount = notional * couponRate * accrualTime.get();
                survival = cdsForwardZeroPrice(spreadCurve, today, accEndDate.plusDays(obsOffset));
                discount = cdsForwardZeroPrice(discountCurve, today, payDate);
                myPv = amount * survival * discount;

                /**
                 * also need to calculate accrual pv
                 */
                cdsAccrualOnDefaultPVWithTimeLine(today, stepInDate.plusDays(obsOffset),
                        accStartDate.plusDays(obsOffset),
                        accEndDate.plusDays(obsOffset),
                        amount,
                        discountCurve,
                        spreadCurve,
                        tl,
                        accrual);

                myPv += accrual.get();
                break;
            }

            default:
                throw new RuntimeException("Invalid accrual payment type");
        }

        pv.set(myPv);
        return ReturnStatus.SUCCESS;
    }

    private ReturnStatus cdsAccrualOnDefaultPVWithTimeLine(LocalDate today,
                                                           LocalDate stepinDate,
                                                           LocalDate startDate,
                                                           LocalDate endDate,
                                                           double amount,
                                                           TCurve discountCurve,
                                                           TCurve spreadCurve,
                                                           TDateList criticalDates,
                                                           DoubleHolder pv) {
        double myPv = 0;

        double t, s0, s1, df0, df1, accRate;
        LocalDate subStartDate;
        TDateList tl = null;

        assert (endDate.isAfter(startDate));
        assert (discountCurve != null);
        assert (spreadCurve != null);
        assert (pv != null);

        /**
         ** Timeline is points on the spreadCurve between startDate and endDate,
         ** combined with points from the discCurve, plus
         ** the startDate and endDate.
         */

        if (criticalDates != null) {
            List<LocalDate> dateList = Arrays.asList(criticalDates.dateArray);
            List<LocalDate> truncDates = cdsTruncateTimeLine(criticalDates, startDate, endDate);
            //if(!(date.isBefore(startDate) || date.isAfter(endDate)))

            tl = new TDateList(truncDates);
        } else {
            tl = cdsRiskyTimeLine(startDate, endDate, discountCurve, spreadCurve);
        }

        /**
         * the integration - we can assume flat forwards between points on the timeline
         * this is true for both curves
         *
         * we are integrating -Zt dS/dt where Z is the discount factor and S is the
         * survival probability and t is the accrual time
         *
         * assuming flat forwards on each part of the integration, this is an exact
         * integral
         */

        subStartDate = stepinDate.isAfter(startDate) ? stepinDate : startDate;
        t = (double) (startDate.periodUntil(endDate, ChronoUnit.DAYS)) / 365.;
        accRate = amount / t;
        s0 = cdsForwardZeroPrice(spreadCurve, today, subStartDate);
        df0 = cdsForwardZeroPrice(discountCurve, today, today.isAfter(subStartDate) ? today : subStartDate);

        for (int i = 1; i < tl.fNumItems; i++) {
            double lambda, fwdRate, thisPv = 0;
            double t0, t1, lambdaFwdRate;

            if (tl.dateArray[i].isBefore(stepinDate) || tl.dateArray[i].isEqual(stepinDate))
                continue;

            s1 = cdsForwardZeroPrice(spreadCurve, today, tl.dateArray[i]);
            df1 = cdsForwardZeroPrice(discountCurve, today, tl.dateArray[i]);

            t0 = (startDate.periodUntil(subStartDate, ChronoUnit.DAYS) + 0.5) / 365.;
            t1 = (startDate.periodUntil(tl.dateArray[i], ChronoUnit.DAYS) + 0.5) / 365.;
            t = t1 - t0;


            /**
             * Markit proposed Fix
             * Some of the division of the original ISDA model can be removed
             * lambda = log( s0 / s1) / t = (log(s0) - log(s1)) / t
             * fwdRate = log(df0 / df) / t = (log(df0) - log(df1)) / t
             * Divisions by t can be absorbed by later formulas as well.
             */

            lambda = Math.log(s0) - Math.log(s1);
            fwdRate = Math.log(df0) - Math.log(df1);
            lambdaFwdRate = lambda + fwdRate + 1.0e-50;

            /**
             * If lambdafwdRate is extremely small, original calculation generates big noise on computer
             * due to the small denominators.
             * In this case, Talyor expansion is employed to remove lambdafwdRate from denomintors
             * so that numerical noise is signicantly reduced.
             */
            if (Math.abs(lambdaFwdRate) > 1e-4) {

                /*This is the original formula which contains an integral*/
                thisPv = lambda * accRate * s0 * df0 * (
                        (t0 + t / (lambdaFwdRate)) / (lambdaFwdRate) -
                                (t1 + t / (lambdaFwdRate)) / (lambdaFwdRate) *
                                        s1 / s0 * df1 / df0);

                /** This is the accrual on default formula fix
                 thisPv  = lambda * accRate * s0 * df0 * t * ( \
                 1.0 / lambdafwdRate / lambdafwdRate - \
                 (1.0 + 1.0 / lambdafwdRate) / lambdafwdRate * \
                 s1 / s0 * df1 / df0);
                 */
            } else {
                /**
                 This is the numerical fix corresponding to the original formula
                 */
                final double lambdaAccRate = lambda * s0 * df0 * accRate * 0.5;
                final double thisPv1 = lambdaAccRate * (t0 + t1);

                final double lambdaAccRateLamdaFwdRate = lambdaAccRate * lambdaFwdRate / 3.;
                final double thisPv2 = -lambdaAccRateLamdaFwdRate * (t0 + 2. * t1);

                final double lambdaAccRateLamdaFwdRate2 = lambdaAccRateLamdaFwdRate * lambdaFwdRate * .25;
                final double thisPv3 = lambdaAccRateLamdaFwdRate2 * (t0 + 3. * t1);

                final double lambdaAccRateLamdaFwdRate3 = lambdaAccRateLamdaFwdRate2 * lambdaFwdRate * .2;
                final double thisPv4 = -lambdaAccRateLamdaFwdRate3 * (t0 + 4. * t1);

                final double lambdaAccRateLamdaFwdRate4 = lambdaAccRateLamdaFwdRate3 * lambdaFwdRate / 6.;
                final double thisPv5 = lambdaAccRateLamdaFwdRate4 * (t0 + 5. * t1);

                /** This is the numerical fix along with accrual on default model fix
                 const double lambdaAccRate = lambda * s0 * df0 * accRate * t;
                 const double thisPv1 = lambdaAccRate * 0.5;

                 const double lambdaAccRateLamdaFwdRate = lambdaAccRate * lambdafwdRate;
                 const double thisPv2 = -lambdaAccRateLamdaFwdRate / 3.;

                 const double lambdaAccRateLamdaFwdRate2 = lambdaAccRateLamdaFwdRate * lambdafwdRate;
                 const double thisPv3 = lambdaAccRateLamdaFwdRate2 * .125;

                 const double lambdaAccRateLamdaFwdRate3 = lambdaAccRateLamdaFwdRate2 * lambdafwdRate;
                 const double thisPv4 = -lambdaAccRateLamdaFwdRate3 / 30.;

                 const double lambdaAccRateLamdaFwdRate4 = lambdaAccRateLamdaFwdRate3 * lambdafwdRate;
                 const double thisPv5 = lambdaAccRateLamdaFwdRate4 / 144.;
                 */

                thisPv += thisPv1;
                thisPv += thisPv2;
                thisPv += thisPv3;
                thisPv += thisPv4;
                thisPv += thisPv5;
            }

            myPv += thisPv;
            s0 = s1;
            df0 = df1;
            subStartDate = tl.dateArray[i];
        }

        pv.set(myPv);
        return ReturnStatus.SUCCESS;
    }

    public static List<LocalDate> cdsTruncateTimeLine(TDateList criticalDates, LocalDate startDate, LocalDate endDate) {
        LocalDate[] startEndDate = new LocalDate[2];
        startEndDate[0] = startDate;
        startEndDate[1] = endDate;

        assert (endDate.isAfter(startDate));
        TDateList tl = cdsDateListAddDates(criticalDates, 2, startEndDate);
        if (tl == null)
            throw new RuntimeException("cdsTruncateTimeline");

        List<LocalDate> retList = new ArrayList<>();
        for (LocalDate date : tl.dateArray)
            if (!(date.isBefore(startDate) || date.isAfter(endDate)))
                retList.add(date);
        return retList;
    }


    public static TDateList cdsRiskyTimeLine(LocalDate startDate, LocalDate endDate, TCurve discountCurve, TCurve spreadCurve) {
        TDateList tl = null;
        LocalDate[] dates = null;

        assert (discountCurve != null);
        assert (spreadCurve != null);
        assert (endDate.isAfter(startDate));

        /**
         * Time line is points on the spread curve between the start date and end date
         * plus the start date and end date, plus the critical dates
         */
        tl = new TDateList(discountCurve);

        /**
         * Code for JpmcdsDatesFromCurve
         */
        dates = new LocalDate[spreadCurve.dates.length];
        for (int i = 0; i < spreadCurve.dates.length; i++)
            dates[i] = spreadCurve.dates[i];

        tl = cdsDateListAddDatesFreeOld(tl, spreadCurve.dates.length, dates);
        tl = cdsDateListAddDatesFreeOld(tl, 1, new LocalDate[]{startDate});
        tl = cdsDateListAddDatesFreeOld(tl, 1, new LocalDate[]{endDate});

        /**
         * Remove dates strictly before and after end dates
         */

        List<LocalDate> dateList = Arrays.asList(tl.dateArray);
        List<LocalDate> truncatedList = new ArrayList<LocalDate>();

        for (LocalDate date : dateList) {
            if (!(date.isBefore(startDate) || date.isAfter(endDate)))
                truncatedList.add(date);
        }

        LocalDate[] truncatedDateList = truncatedList.toArray(new LocalDate[0]);
        tl = new TDateList(truncatedDateList.length, truncatedDateList);

        return tl;

    }

        /*
    public static double cdsZeroRate(TCurve zeroCurve, LocalDate date) {
        int exact;
        double rate = 0.0;

        if (zeroCurve.dates.length == 1) {
            exact = 0;
            rate = zcRateCC(zeroCurve, exact);
            return rate;
        }

        exact = Arrays.binarySearch(zeroCurve.dates, date, null);
        if (exact >= 0) {
            rate = zcRateCC(zeroCurve, exact);
            return rate;
        }

        //date before start of zero dates
        if (date.isBefore(zeroCurve.dates[0])) {
            rate = zcRateCC(zeroCurve, 0);
            return rate;
        }

        // date after end of zero dates
        if (date.isAfter(zeroCurve.dates[zeroCurve.dates.length - 1])) {
            if (zeroCurve.dates.length == 1)
                rate = zcRateCC(zeroCurve, 0);
            else {
                // extrapolate using last flat segment of the curve
                int length = zeroCurve.dates.length;
                int lo = length - 2;
                int high = length - 1;
                rate = zcInterpRate(zeroCurve, date, lo, high);
                return rate;
            }
        } else {
            // date between start and end of zeroDates
            TreeSet<LocalDate> set = new TreeSet<LocalDate>(Arrays.asList(zeroCurve.dates));

            LocalDate loDate = set.floor(date);
            LocalDate hiDate = set.ceiling(date);

            int lo, high;
            lo = Arrays.binarySearch(zeroCurve.dates, loDate);
            high = Arrays.binarySearch(zeroCurve.dates, hiDate);

            rate = zcInterpRate(zeroCurve, date, lo, high);
            return rate;
        }

        return rate;
    }
    */


    //TODO - this whole logic needs to be looked at again
    private TDateList dateListMakeRegular(LocalDate startDate, LocalDate endDate, TDateInterval dateInterval, TStubMethod stubType) {

        TDateInterval multiInterval;
        int numIntervals = 0;
        int totalDates = 0;
        LocalDate date;
        int numTmpDates = 100;
        LocalDate[] tmpDates = new LocalDate[numTmpDates];

        TDateList dl = null;
        int i;
        if (!(stubType.stubAtEnd)) {
            /*front stub - so we start at end and work backwords*/
            numIntervals = 0;
            date = endDate;

            i = tmpDates.length;

            while (date.isAfter(startDate)) {
                if (i == 0) {
                    dl = cdsDateListAddDatesFreeOld(dl, numTmpDates, tmpDates);
                    i = numTmpDates;
                }

                --i;
                --numIntervals;
                ++totalDates;

                tmpDates[i] = date;

                multiInterval = new TDateInterval(dateInterval.prd * numIntervals, dateInterval.periodType, 0);
                date = dtFwdAny(endDate, multiInterval);

            }

            assert (totalDates > 0);
            assert (date.isBefore(startDate) || date.isEqual(startDate));

            if (date.isEqual(startDate) || totalDates == 1 || !stubType.longStub) {
                /*dont change existing tmpDates but need to add startDate*/
                if (i == 0) {
                    dl = cdsDateListAddDatesFreeOld(dl, numTmpDates, tmpDates);
                    i = numTmpDates;
                }
                --i;
                ++totalDates;
                tmpDates[i] = startDate;
            } else {
                assert (!stubType.stubAtEnd && stubType.longStub);
                assert (date.isBefore(startDate));

                // the existing date in tmpDates[] should be changed to be the start date
                tmpDates[i] = startDate;
            }

            List<LocalDate> datesToAdd = new ArrayList<LocalDate>();
            for (int k = 0; k < numTmpDates - i; k++) {
                datesToAdd.add(tmpDates[i + k]);
            }

            dl = cdsDateListAddDatesFreeOld(dl, numTmpDates - i, datesToAdd.toArray(new LocalDate[0]));
        } else {
            /* back stub - so we start and startDate and work forwards */
            numIntervals = 0;
            i = -1;
            date = startDate;
            while (date.isBefore(endDate)) {
                ++i;
                ++totalDates;
                if (i == numTmpDates) {
                    dl = cdsDateListAddDatesFreeOld(dl, numTmpDates, tmpDates);

                    i = 0;
                }

                ++numIntervals;
                assert (i < numTmpDates);
                tmpDates[i] = date;

                multiInterval = new TDateInterval(dateInterval.prd * numIntervals, dateInterval.periodType, 0);
                date = dtFwdAny(startDate, multiInterval);

            }

            assert (totalDates > 0);
            assert (date.isAfter(endDate) || date.isEqual(endDate));
            if (date.isEqual(endDate) || totalDates == 1 || stubType.stubAtEnd && !stubType.longStub) {
                /* don't change existing tmpDates[] but need to add endDate */
                ++i;
                ++totalDates;

                if (i == numTmpDates) {
                    dl = cdsDateListAddDatesFreeOld(dl, numTmpDates, tmpDates);
                    i = 0;
                }

                tmpDates[i] = endDate;
            } else {

                assert (stubType.stubAtEnd && stubType.longStub);
                assert (date.isAfter(endDate));

               /* the existing date in tmpDates[] should be changed to be
               the end date */
                tmpDates[i] = endDate;

            }

            /* now add from tmpDates[0] to tmpDates[i] to the date list */
            dl = cdsDateListAddDatesFreeOld(dl, i + 1, tmpDates);
        }

        assert (totalDates >= 2);
        assert (dl.fNumItems == totalDates);
        return dl;
    }

    public static TDateList cdsDateListAddDatesFreeOld(TDateList dl, int numItems, LocalDate[] array) {
        TDateList output = null;
        output = cdsDateListAddDates(dl, numItems, array);
        cdsFreeDateList(dl);

        return output;
    }

    public static void cdsFreeDateList(TDateList dl) {
        if (dl != null) {
            dl.dateArray = null;
            dl = null;
        }
    }

    public static TDateList cdsDateListAddDates(TDateList dl, int numItems, LocalDate[] array) {
        TDateList tmp = new TDateList();
        TDateList result = null;

        if (dl == null)
            result = cdsNewDateListFromDates(array);
        else if (numItems <= 0)
            result = cdsCopyDateList(dl);
        else if (dl.fNumItems == 0 && numItems == 0)
            result = new TDateList();
        else {
            /* TODO - exclude duplicates ??*/
            Set<LocalDate> dlDates = new HashSet<LocalDate>(dl.dateArray.length);
            dlDates.addAll(Arrays.asList(dl.dateArray));

            Set<LocalDate> datesToAdd = new HashSet<LocalDate>(array.length);
            datesToAdd.addAll(Arrays.asList(array));

            Set<LocalDate> allDates = new HashSet<LocalDate>();
            allDates.addAll(dlDates);
            allDates.addAll(datesToAdd);

            List<LocalDate> list = new ArrayList<>(allDates.size());
            list.addAll(allDates);

            Collections.sort(list);
            result = new TDateList(list.size());
            for (int i = 0; i < list.size(); i++)
                result.dateArray[i] = list.get(i);

            /*
            int totalItems = dl.fNumItems + numItems;
            int i =0, j = 0, k = 0;
            result = cdsNewEmptyDateList(totalItems);

            while (i < dl.fNumItems && j < numItems) {
                if (dl.dateArray[i].isEqual( array[j])) {
                    // exclude duplicates
                    ++j;
                    --totalItems;
                } else if (dl.dateArray[i].isBefore(array[j])) {
                    result.dateArray[k] = dl.dateArray[i];
                    ++i;
                    ++k;
                } else {
                    assert (dl.dateArray[i].isAfter(array[j]));
                    result.dateArray[k] = array[j];
                    ++j;
                    ++k;
                }
            }

            if (i < dl.fNumItems) {
               int n = dl.fNumItems - i;
               //Copy array
               for (int l = 0; l < n; l++) {
                   result.dateArray[k] = dl.dateArray[i];
                   ++k;
                   ++i;
               }
            }

            if (j < numItems) {
                int n = numItems - j;
                for (int l = 0; l < n; l++) {
                    result.dateArray[k] = array[j];
                    ++k;
                    ++j;
                }
            }

            assert ( k == totalItems);
            result.fNumItems = totalItems;
            */
        }

        return result;
    }

    public static TDateList cdsCopyDateList(TDateList dl) {
        return dl.clone();
    }

    public static TDateList cdsNewDateListFromDates(LocalDate[] dates) {
        TDateList dateList = new TDateList(dates);
        return dateList;

    }


}
