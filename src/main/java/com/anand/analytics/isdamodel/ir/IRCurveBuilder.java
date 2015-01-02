package com.anand.analytics.isdamodel.ir;

import com.anand.analytics.isdamodel.date.HolidayCalendar;
import com.anand.analytics.isdamodel.domain.*;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.utils.*;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.List;

import static com.anand.analytics.isdamodel.domain.TDateFunctions.IS_BETWEEN;
import static com.anand.analytics.isdamodel.domain.TDateFunctions.cdsDayCountFraction;
import static com.anand.analytics.isdamodel.domain.TDateFunctions.dateFromDateAndOffset;

/**
 * Created by anand on 12/27/14.
 */
public class IRCurveBuilder {
    private final static Logger logger = Logger.getLogger(IRCurveBuilder.class);

    public static TCurve buildIRZeroCurve(
            LocalDate valueDate,
            char[] instrumentNames,
            LocalDate[] dates,
            double[] rates,
            DayCount mmDCC,
            long fixedSwapFreq,
            long floatSwapFreq,
            DayCount fixedSwapDayCount,
            DayCount floatSwapDayCount,
            TBadDayConvention badDayConvention,
            HolidayCalendar calendar
    ) throws CdsLibraryException {
        final List<LocalDate> cashDates = new ArrayList<LocalDate>();
        final List<LocalDate> swapDates = new ArrayList<LocalDate>();

        final List<Double> cashRates = new ArrayList<Double>();
        final List<Double> swapRates = new ArrayList<>();

        final List<String> validInstruments = TInstrumentType.getValuesAsList();

        for (int i = 0; i < instrumentNames.length; i++) {
            String instr = String.valueOf(instrumentNames[i]).toUpperCase();
            if (!validInstruments.contains(instr))
                throw new CdsLibraryException("Invalid InstrumentType");
            TInstrumentType instrumentType = TInstrumentType.getInstrumentType(instr);
            switch (instrumentType) {
                case M:
                    cashDates.add(dates[i]);
                    cashRates.add(rates[i]);
                    break;
                case S:
                    swapDates.add(dates[i]);
                    swapRates.add(rates[i]);
                    break;
                default:
                    throw new CdsLibraryException("Unknown instrument type");
            }
        }

        final LocalDate[] cashDatesArr = cashDates.toArray(new LocalDate[0]);
        final LocalDate[] swapDatesArr = swapDates.toArray(new LocalDate[0]);

        final double[] cashRatesArr = ArrayUtils.toPrimitive(cashRates.toArray(new Double[0]));
        final double[] swapRatesArr = ArrayUtils.toPrimitive(swapRates.toArray(new Double[0]));

        /* Cash instruments */
        ZeroCurve zCurveCash = new ZeroCurve(valueDate, DayCountBasis.ZC_DEFAULT_BASIS, DayCount.ACT_365F);
        TCurve discountZC = null;


        zCurveCash.addRates(cashDatesArr, cashRatesArr, mmDCC);
        buildZeroCurveSwap(zCurveCash,
                discountZC,
                swapDatesArr,
                swapRatesArr,
                swapDatesArr.length,
                fixedSwapFreq,
                floatSwapFreq,
                fixedSwapDayCount,
                floatSwapDayCount,
                3,
                badDayConvention,
                calendar);
        return zCurveCash.toTCurve();
    }

    private static TCurve buildZeroCurveSwap(ZeroCurve zeroCurve,
                                             TCurve discountZC,
                                             LocalDate swapDates[],
                                             double swapRates[],
                                             int numSwaps,
                                             long fixedSwapFreq,
                                             long floatSwapFreq,
                                             DayCount fixedSwapDayCount,
                                             DayCount floatSwapDayCount,
                                             int fwdLength,
                                             TBadDayConvention badDayConvention,
                                             HolidayCalendar calendar) throws CdsLibraryException {

        TStubPos stubPos = TStubPos.DEFAULT_AUTO;
        TCurve tcSwaps = null;
        if (numSwaps == 0) {
            tcSwaps = zeroCurve.toTCurve().clone();
            return tcSwaps;
        }

        checkSwapInputs(zeroCurve.toTCurve(),
                discountZC,
                swapDates,
                swapRates,
                numSwaps,
                fixedSwapFreq,
                floatSwapFreq,
                fixedSwapDayCount,
                floatSwapDayCount,
                fwdLength,
                badDayConvention,
                calendar);

        LocalDate lastStubDate;
        if (zeroCurve.getfNumItems() < 1) {
            lastStubDate = zeroCurve.getValueDate();
        } else {
            lastStubDate = zeroCurve.getDates()[zeroCurve.getfNumItems() - 1];
        }

        int offset = 0;
        List<LocalDate> offsetSwapDatesList = new ArrayList<>();
        List<Double> offsetSwapRatesList = new ArrayList<>();

        while (numSwaps > 0 && swapDates[offset].isBefore(lastStubDate)) {
            offset++;
            numSwaps--;

        }


        //TODO - replace this with System.arraycopy
        //region
        for (int i = offset; i < swapDates.length; i++) {
            offsetSwapDatesList.add(swapDates[i]);
            offsetSwapRatesList.add(swapRates[i]);
        }

        LocalDate[] offsetSwapDates = offsetSwapDatesList.toArray(new LocalDate[0]);
        double[] offsetSwapRates = ArrayUtils.toPrimitive(offsetSwapRatesList.toArray(new Double[0]));
        //endregion

        TInterpData tInterpData = null;
        TBadDayList tBadDayList = null;

        if (numSwaps > 0) {
            zcAddSwaps(
                    zeroCurve,
                    discountZC,
                    offsetSwapDates,
                    offsetSwapRates,
                    numSwaps,
                    fixedSwapFreq,
                    floatSwapFreq,
                    fixedSwapDayCount,
                    floatSwapDayCount,
                    TInterpType.FLAT_FORWARDS,
                    tInterpData,
                    tBadDayList,
                    badDayConvention,
                    stubPos,
                    calendar


            );
        }
        return null;
    }

    private static void zcAddSwaps(ZeroCurve zeroCurve,
                                   TCurve discountZC,
                                   LocalDate[] inDates,
                                   double[] inRates,
                                   int numSwaps,
                                   long fixedSwapFreq,
                                   long floatSwapFreq,
                                   DayCount fixedSwapDayCount,
                                   DayCount floatSwapDayCount,
                                   TInterpType interpType,

                                   TInterpData tInterpData,
                                   TBadDayList tBadDayList,
                                   TBadDayConvention badDayConvention,
                                   TStubPos stubPos,
                                   HolidayCalendar calendar) throws CdsLibraryException {
        try {
            Validate.notNull(zeroCurve, "zeroCurve == null");
            Validate.isTrue(zeroCurve.getfNumItems() > 0, "zeroCurve.getfNumItems < 1");

            boolean cond = !(tBadDayList != null && !(badDayConvention.equals(TBadDayConvention.NONE)));
            Validate.isTrue(cond, "Invalid badDayConv / badDayList");

            TSwapDate[] swapDates = null;
            if (tBadDayList != null) {
                //this should never be true for us
                //TODO - may code later
                //use the holiday list to define the adjustments
            } else {
                swapDates = swapDatesNewFromOriginal(
                        zeroCurve.getValueDate(),
                        fixedSwapFreq,
                        inDates,
                        numSwaps,
                        tBadDayList,
                        badDayConvention,
                        calendar
                );

            }
            Validate.notNull(swapDates, "swapDates == null");

            double[] swapRates = inRates;
            boolean oneAlreadyAdded = false;

            /**
             * Add individual swap instruments
             */
            for (int i = 0; i < swapDates.length; i++) {
                TSwapDate tSwapDate = swapDates[i];
                /**
                 * Add those beyond stub zero curve
                 */
                if (tSwapDate.getAdjustedDate().isAfter(zeroCurve.getDates()[zeroCurve.getfNumItems() - 1])) {
                    /**
                     * Check if optimization ok. Note linear forwards not OK because
                     * they must include intermed. forwards
                     */
                    if (oneAlreadyAdded &&
                            discountZC == null &&
                            swapRates[i - 1] != 0 &&
                            swapDates[i - 1].getAdjustedDate().isEqual(zeroCurve.getDates()[zeroCurve.getfNumItems() - 1]) &&
                            tSwapDate.getPreviousDate().isEqual(swapDates[i - 1].getOriginalDate()) &&
                            tSwapDate.isOnCycle() &&
                            !(interpType.equals(TInterpType.LINEAR_FORWARDS))) {
                        /**
                         * Optimization - compute from last
                         */
                    } else {
                        ReturnStatus status = zcAddSwap(zeroCurve,
                                discountZC,
                                1.0,
                                tSwapDate.getOriginalDate(),
                                tSwapDate.isOnCycle(),
                                swapRates[i],
                                fixedSwapFreq,
                                floatSwapFreq,
                                fixedSwapDayCount,
                                floatSwapDayCount,
                                interpType,
                                tInterpData,
                                tBadDayList,
                                badDayConvention,
                                stubPos,
                                calendar);
                        if (status.equals(ReturnStatus.FAILURE))
                            throw new CdsLibraryException("Error adding swap with original date " + tSwapDate.getOriginalDate());

                        oneAlreadyAdded = true;
                    }
                }
            }

        } catch (Exception ex) {
            logger.error(ex);
            throw new CdsLibraryException(ex.getMessage());
        }
    }

    /**
     * Adds a single swap instrument to a ZCurve
     * <p/>
     * ZCurve is updated to contain swap information (with points at each of the coupon dates,
     * as well as maturity date)
     *
     * @param zeroCurve
     * @param discountZC
     * @param price
     * @param matDate
     * @param onCycle
     * @param rate
     * @param fixedSwapFreq
     * @param floatSwapFreq
     * @param fixedSwapDayCount
     * @param floatSwapDayCount
     * @param interpType
     * @param tInterpData
     * @param tBadDayList
     * @param badDayConvention
     * @param stubPos
     * @param calendar
     * @return
     */
    private static ReturnStatus zcAddSwap(ZeroCurve zeroCurve,
                                          TCurve discountZC,
                                          double price,
                                          LocalDate matDate,
                                          boolean onCycle,
                                          double rate,
                                          long fixedSwapFreq,
                                          long floatSwapFreq,
                                          DayCount fixedSwapDayCount,
                                          DayCount floatSwapDayCount,
                                          TInterpType interpType,
                                          TInterpData tInterpData,
                                          TBadDayList tBadDayList,
                                          TBadDayConvention badDayConvention,
                                          TStubPos stubPos,
                                          HolidayCalendar calendar) {
        try {
            /**
             * If floating side at par
             */
            if (discountZC == null) {
                boolean isEndStub;
                /**
                 * Here we must pass in the unadjusted matDate, so that the
                 * cashflow dates are correctly generated
                 */

                TDateInterval ivl = CdsFunctions.cdsFreq2TDateInterval(fixedSwapFreq);
                if (onCycle) {
                    isEndStub = true;
                } else {
                    BooleanHolder isEndStubHolder = new BooleanHolder(false);
                    if (CdsFunctions.cdsIsEndStub(zeroCurve.getValueDate(),
                            matDate,
                            ivl,
                            stubPos,
                            isEndStubHolder).equals(ReturnStatus.FAILURE)) {
                        logger.error("Error calculating endStub");
                        return ReturnStatus.FAILURE;
                    }

                    isEndStub = isEndStubHolder.get();
                }

                TCashFlow[] cashFlowList = zcGetSwapCashFlowList(
                        zeroCurve.getValueDate(),
                        matDate,
                        isEndStub,
                        rate,
                        ivl,
                        fixedSwapDayCount,
                        tBadDayList,
                        badDayConvention,
                        calendar
                );
                /**
                 * Adjust matDate to be a good business day
                 */
                LocalDate adjustedMatDate = calendar.getNextBusinessDay(matDate, badDayConvention);

                /**
                 * Add rate implied by cash flow list to the zeroCurve
                 */
                zcAddCashFlowList(zeroCurve,
                        cashFlowList,
                        price,
                        adjustedMatDate,
                        interpType,
                        tInterpData);

            }
        } catch (Exception ex) {
            logger.error(ex);
            return ReturnStatus.FAILURE;
        }
        return null;
    }

    /**
     * Adds information represented by a list of cash flows to a zero curve
     * Any cash flows which are already covered by the zero curve are
     * discounted at rates derived from the zero curve
     *
     * Cash flows beyond the zero curve imply discount factors, which are added to the zero curve
     * If there is more than one such cash flow, several points are added to the curve,
     * which are calculated by using an iterative root-finding secant method
     * where the discount factor for the last cash flow is guessed (and the other discount factors are
     * implied by interpolation) where the current price = net present value of all cash flows
     *
     * The zero curve is updated to reflect the cash flows. A point is added for every cash flow, if
     * not already in ZCurve list. For linear forwards, all interpolated points are returned, e.g.
     * for 1 month forwards in an annual market, monthly points will be returned, not just yearly.
     *
     * Notes: date may be set for non-linear forward interpolation methods to a date to be added to the
     * zero curve. This allows production of a curve with "nice" dates
     *
     * @param zc
     * @param cfl
     * @param price
     * @param date
     * @param interpType
     * @param interpData
     */
    private static void zcAddCashFlowList(ZeroCurve zc,
                                          TCashFlow[] cfl,
                                          double price,
                                          LocalDate date,
                                          TInterpType interpType,
                                          TInterpData interpData)
        throws CdsLibraryException {

        int firstUncoverd; /* index in cfl of 1st uncovered c.f */

        /* add at last c.f if not set up */
        if (date ==  null)
            date = cfl[cfl.length - 1].getfDate();

        if (zc.getfNumItems() <= 0) {
            firstUncoverd = 0;
            interpType = TInterpType.LINEAR_INTERP;
        } else {
            /* last date in zCurve */
            LocalDate lastZcDate = zc.getDates()[zc.getfNumItems() - 1];
            if (date.isEqual(lastZcDate) || date.isBefore(lastZcDate)) {
                throw new CdsLibraryException("Date to add already covered");
            }

            firstUncoverd = cfl.length - 1;
            if (firstUncoverd < 0 ||
                    cfl[firstUncoverd].getfDate().isBefore(lastZcDate)) {
                throw new CdsLibraryException("No cash flows in list beyond zeroCurve - nothing to add");

            }

            while (firstUncoverd >= 0 && cfl[firstUncoverd].getfDate().isAfter(lastZcDate))
                firstUncoverd--;    // decrement until covered

            //move upto first not covered
            firstUncoverd++;

            //Calc npv of covered cash flows
            if (firstUncoverd > 0) {
                DoubleHolder sumNpv = new DoubleHolder();
                zcPresentValueCFL(zc, cfl, 0, firstUncoverd - 1, interpType, interpData);
            }


        }

    }

    /**
     * Calculate net present value of cash flow list
     *
     * @param zc
     * @param cfl
     * @param iLo
     * @param iHi
     * @param interpType
     * @param interpData
     *
     * @return
     */
    private static double zcPresentValueCFL(ZeroCurve zc,
                                                  TCashFlow[] cfl,
                                                  int iLo,
                                                  int iHi,
                                                  TInterpType interpType,
                                                  TInterpData interpData
                                                  ) throws CdsLibraryException {

        double sumPv = 0.0;
        int i;                          //loops over cash flows [iLo..iHi]
        int j = 0;                      //loops over zc entries

        if (iLo < 0) {
            throw new CdsLibraryException("iLo < 0");
        }
        if (iLo > iHi)
            throw new CdsLibraryException("iLo > iHi");

        //TODO - check this
        if (cfl.length <= iHi) {
            throw new CdsLibraryException("cfl.length <= iHi");
        }

        for (i = iLo; i <= iHi; i++) {
            double amt = cfl[i].getAmount();
            LocalDate date = cfl[i].getfDate();

            double pv;
            //push j upto cf date
            while (j < zc.getfNumItems() && zc.getDates()[j].isBefore(date))
                j++;

            if (j < zc.getfNumItems() && zc.getDates()[j].isEqual(date)) {
                //found exact date in zc
                pv = zc.getDiscs()[j] * amt;
            } else {
                pv = zcPresentValue(zc, amt, date, interpType, interpData);
            }
        }
        return sumPv;
    }

    /**
     * Calculates the npv of a cash flow (a payment at a given date in the future)
     * @param zc
     * @param price
     * @param date
     * @param interpType
     * @param interpData
     * @return
     * @throws CdsLibraryException
     */
    private static double zcPresentValue(ZeroCurve zc,
                                         double price,
                                         LocalDate date,
                                         TInterpType interpType,
                                         TInterpData interpData)
    throws CdsLibraryException {
        return 0;
    }

    private static TCashFlow[] zcGetSwapCashFlowList(LocalDate valueDate,
                                                     LocalDate matDate,
                                                     boolean stubAtEnd,
                                                     double rate,
                                                     TDateInterval interval,
                                                     DayCount dayCountConv,
                                                     TBadDayList badDayList,
                                                     TBadDayConvention badDayConvention,
                                                     HolidayCalendar calendar) throws CdsLibraryException {
        TCashFlow[] tCashFlows;
        if (rate == 0.0) {
            tCashFlows = new TCashFlow[1];
            double amount = 1;
            LocalDate adjustedDate = calendar.getNextBusinessDay(matDate, badDayConvention);
            TCashFlow tCashFlow = new TCashFlow(adjustedDate, amount);
            tCashFlows[0] = tCashFlow;
            return tCashFlows;
        }

        LocalDate[] dateList = zcGetSwapCouponDateList(valueDate,
                matDate,
                stubAtEnd,
                interval,
                badDayList,
                badDayConvention,
                calendar);

        tCashFlows = new TCashFlow[dateList.length];
        LocalDate prevDate = valueDate;
        for (int i = 0; i < dateList.length; i++) {
            LocalDate cDate = dateList[i];

            DoubleHolder yearFraction = new DoubleHolder();
            if(cdsDayCountFraction(prevDate, cDate, dayCountConv, yearFraction).equals(ReturnStatus.FAILURE))
                throw new CdsLibraryException("Error calculating dayCountFraction");
            double amount = rate * yearFraction.get();
            TCashFlow tCashFlow = new TCashFlow(cDate, amount);
            tCashFlows[i] = tCashFlow;
            prevDate = cDate;
        }

        /** Add principal */
        double amount = tCashFlows[tCashFlows.length - 1].getAmount();
        amount += 1.0;

        tCashFlows[tCashFlows.length - 1].setAmount(amount);
        return new TCashFlow[0];
    }

    /**
     * Makes a date list for all coupons associated w/ a swap instrument
     *
     * Only glitch is possible inclusion of a stub date, which is necessary if the
     * maturity date isnt an integral number of frequency intervales away, e.g. a
     * swap date 5 years and 1 month from the value date, which would have a stub
     * date 1 month from now, followed by coupons every year from then
     *
     * R
     * @param valueDate
     * @param matDate
     * @param stubAtEnd
     * @param interval
     * @param badDayList
     * @param badDayConvention
     * @param calendar
     * @return
     */
    private static LocalDate[] zcGetSwapCouponDateList(LocalDate valueDate,
                                                       LocalDate matDate,
                                                       boolean stubAtEnd,
                                                       TDateInterval interval,
                                                       TBadDayList badDayList,
                                                       TBadDayConvention badDayConvention,
                                                       HolidayCalendar calendar) throws CdsLibraryException {

        /**
         * If the maturity date is onCycle, then the stub is at end, because
         * we are counting forward from the maturity date
         */
        LocalDate[] dl = cdsNewPayDates(valueDate, matDate, interval, stubAtEnd);

        /**
         * Now adjust for bad days
         */
        dl = calendar.adjustBusinessDays(dl, badDayConvention);
        return dl;
    }

    /**
     * Allocates a new DateList by calling cdsNewDateList and then removing the start date
     * @param startDate
     * @param matDate
     * @param payInterval
     * @param stubAtEnd
     * @return
     * @throws CdsLibraryException
     */
    private static LocalDate[] cdsNewPayDates(LocalDate startDate, LocalDate matDate, TDateInterval payInterval, boolean stubAtEnd)
    throws CdsLibraryException{
        LocalDate[] payDates = cdsNewDateList(startDate, matDate, payInterval, stubAtEnd);

        /**
         * Now remove startDate, and move all dates back by one
         *
         */
        int size = payDates.length;
        LocalDate[] newPayDates = new LocalDate[size - 1];
        for (int idx = 0; idx < size - 1; idx++)
            newPayDates[idx] = payDates[idx + 1];

        return newPayDates;
    }

    /**
     * Makes an array of dates from startDate, MaturityDate & interval
     * if (maturityDate - startDate) / interval is not an integer there is a stub
     * if stubAtEnd is set, the stub is placed at the end
     * otherwise, it is placed at the beginning
     *
     * the startDate an maturityDate are always included
     * and are the first and last dates respectively
     *
     * Assuming there is no stub, dates created are of the form
     * baseDate + idx * interval
     *
     * where startIdx <= idx <= Time2Maturity / interval
     * @param startDate
     * @param maturityDate
     * @param interval
     * @param stubAtEnd
     * @return
     */
    private static LocalDate[] cdsNewDateList(LocalDate startDate, LocalDate maturityDate, TDateInterval interval, boolean stubAtEnd)
    throws CdsLibraryException {
        IntHolder numIntervals = new IntHolder();
        IntHolder extraDays = new IntHolder();
        if (stubAtEnd) {
            /** Count forward from the start date*/


            if (CdsFunctions.cdsCountDates(startDate, maturityDate, interval, numIntervals, extraDays).equals(ReturnStatus.FAILURE)) {
                throw new CdsLibraryException("Error in cdsCountDates");
            }

        } else {
            /** Count backward from maturity date */
            TDateInterval intVal = interval;
            intVal.setPrd(-interval.prd);
            if (CdsFunctions.cdsCountDates(startDate, maturityDate, intVal, numIntervals, extraDays).equals(ReturnStatus.FAILURE)) {
                throw new CdsLibraryException("Error in cdsCountDates");
            }
        }

        int numDates;

        if (extraDays.get() > 0)
            numDates = numIntervals.get() + 2;
        else
            numDates = numIntervals.get() + 1;

        return new LocalDate[numDates];
    }


    private static TSwapDate[] swapDatesNewFromOriginal(LocalDate valueDate,
                                                        long freq,
                                                        LocalDate[] swapDates,
                                                        int numDates,
                                                        TBadDayList tBadDayList,
                                                        TBadDayConvention badDayConvention,
                                                        HolidayCalendar calendar) throws CdsLibraryException {
        TSwapDate[] tSwapDates = new TSwapDate[numDates];
        for (int idx = 0; idx < numDates; idx++) {
            TSwapDate tSwapDate = new TSwapDate();
            tSwapDate.setOriginalDate(swapDates[idx]);

            LocalDate adjustedDate = calendar.getNextBusinessDay(swapDates[idx], badDayConvention);
            tSwapDate.setAdjustedDate(adjustedDate);

            setPreviousDateAndOnCycle(valueDate, swapDates[idx], freq, tSwapDate);
            tSwapDates[idx] = (tSwapDate);
        }

        return tSwapDates;

    }

    private static void setPreviousDateAndOnCycle(LocalDate valueDate, LocalDate origDate, long freq, TSwapDate tSwapDate)
            throws CdsLibraryException {
        try {
            TDateInterval interval = CdsFunctions.cdsFreq2TDateInterval(freq);
            IntHolder numItervals = new IntHolder();
            if (valueDate.getDayOfMonth() <= 28 &&
                    origDate.getDayOfMonth() <= 28) {
                /**
                 * We assume we can only be on cycle if date is not on or after the 29th of the month
                 */
                /**
                 * Find out if adjusted date is on cycle
                 */

                IntHolder extraDays = new IntHolder();
                ReturnStatus status = CdsFunctions.cdsCountDates(valueDate,
                        origDate, interval, numItervals, extraDays);
                if (status.equals(ReturnStatus.FAILURE))
                    throw new CdsLibraryException("Error in countDates function");

                tSwapDate.setOnCycle((extraDays.get() == 0));
            } else {
                tSwapDate.setOnCycle(false);
            }

            /**
             * Now compute the prev date. If on cycle - count forward from value date
             * If off cycle count backward from maturity date
             */
            if (tSwapDate.isOnCycle()) {
                LocalDate prevDate = dateFromDateAndOffset(valueDate, interval, numItervals.get() - 1);
                tSwapDate.setPreviousDate(prevDate);
            } else {
                LocalDate prevDate = dateFromDateAndOffset(origDate, interval, -1);
                tSwapDate.setPreviousDate(prevDate);
            }
        } catch (Exception ex) {
            logger.error(ex);
            throw new CdsLibraryException(ex.getMessage());
        }
    }



    private static void checkSwapInputs(TCurve zeroCurve,
                                        TCurve discountZC,
                                        LocalDate[] swapDates,
                                        double[] swapRates,
                                        int numSwaps,
                                        long fixedSwapFreq,
                                        long floatSwapFreq,
                                        DayCount fixedSwapDayCount,
                                        DayCount floatSwapDayCount,
                                        int fwdLength,
                                        TBadDayConvention badDayConvention,
                                        HolidayCalendar calendar) throws CdsLibraryException {


    }

}
