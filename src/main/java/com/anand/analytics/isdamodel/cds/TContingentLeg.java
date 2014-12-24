package com.anand.analytics.isdamodel.cds;


import com.anand.analytics.isdamodel.utils.DoubleHolder;
import com.anand.analytics.isdamodel.utils.ReturnStatus;
import com.anand.analytics.isdamodel.utils.TProtPayConv;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import static com.anand.analytics.isdamodel.cds.TRateFunctions.cdsForwardZeroPrice;
import static com.anand.analytics.isdamodel.cds.TRateFunctions.cdsZeroPrice;


/**
 * Created by Anand on 10/21/2014.
 */
public class TContingentLeg {

    /**
     * Start date of protection. You are protected from the end of this date.
     */
    LocalDate startDate;
    /**
     * End date of protection.
     */
    final LocalDate endDate;
    /**
     * Notional.
     */
    final double notional;
    final TProtPayConv payType;
    /**
     * if TRUE, startDate -= 1
     */
    final boolean protectStart;

    public TContingentLeg(LocalDate startDate, LocalDate endDate, double notional, boolean protectStart, TProtPayConv payType) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.notional = notional;
        this.protectStart = protectStart;
        this.payType = payType;

        /**
         * p->startDate as defined as giving protection from end of startDate.
         * So if we want to protect on the start date, we need to move this
         * date forward by one
         */
        if (protectStart)
            this.startDate = startDate.plusDays(-1);

        assert (this.endDate.isAfter(this.startDate));
    }

    final static double CDS_LOG0_THRESHOLD = 1E-100;

    /**
     * **************************************************************************
     * * Computes the PV of a contingent leg as a whole.
     * *
     * * For each payment period this is the integral of LGD(t) . Z(t) . dS/dt dt
     * * where S is the survival function and LGD is the loss given default
     * * function and Z is the discount function. Discounting is calculated at the
     * * payment date and not at the observation date.
     * **************************************************************************
     */
    public ReturnStatus getPV(LocalDate today,
                              LocalDate valueDate,
                              LocalDate stepInDate,
                              TCurve discountCurve,
                              TCurve spreadCurve,
                              double recoveryRate,
                              DoubleHolder result) {
        double myPv = 0.;
        double valueDatePv = 0.;


        int offset = 0;

        assert (discountCurve != null);
        assert (spreadCurve != null);
        assert (result != null);

        assert (cdsZeroPrice(spreadCurve, endDate) > CDS_LOG0_THRESHOLD);
        assert (cdsZeroPrice(discountCurve, endDate) > CDS_LOG0_THRESHOLD);

        offset = protectStart ? 1 : 0;
        LocalDate startDate = max(this.startDate, stepInDate.minusDays(offset));
        startDate = max(startDate, today.minusDays(offset));

        switch (payType) {
            case PROT_PAY_MAT: {
                DoubleHolder tmp = new DoubleHolder();
                onePeriodIntegralAtPayDate(today,
                        startDate,
                        this.endDate,
                        this.endDate,
                        discountCurve,
                        spreadCurve,
                        recoveryRate,
                        tmp);

                myPv += tmp.get() * notional;
            }
            break;
            case PROT_PAY_DEF: {
                DoubleHolder tmp = new DoubleHolder();
                onePeriodIntegral(today,
                        startDate,
                        endDate,
                        discountCurve,
                        spreadCurve,
                        recoveryRate,
                        tmp);


                myPv += tmp.get() * notional;
            }
            break;

            default:
                throw new RuntimeException("Unknown payment type");

        }

        valueDatePv = cdsForwardZeroPrice(discountCurve, today, valueDate);

        result.set(myPv / valueDatePv);
        return ReturnStatus.SUCCESS;
    }

    /**
     * Computes a one period integral
     */
    private ReturnStatus onePeriodIntegral(LocalDate today,
                                           LocalDate startDate,
                                           LocalDate endDate,
                                           TCurve discountCurve,
                                           TCurve spreadCurve, double recoveryRate, DoubleHolder pv) {

        double myPv = 0;
        double t, s0, s1, df0, df1, loss;
        TDateList tl;

        assert (endDate.isAfter(startDate));
        assert (discountCurve != null);
        assert (spreadCurve != null);
        assert (pv != null);

        if (today.isAfter(endDate)) {
            pv.set(0);
        } else {
            tl = TFeeLeg.cdsRiskyTimeLine(startDate, endDate, discountCurve, spreadCurve);
            /**
             * the integration - we can assume flat forwards between points on
             the timeline - this is true for both curves

             we are integrating -Z dS/dt where Z is the discount factor and
             S is the survival probability

             assuming flat forwards on each part of the integration, this is an
             exact integral
             */
            s1 = cdsForwardZeroPrice(spreadCurve, today, startDate);
            df1 = cdsForwardZeroPrice(discountCurve, today, max(today, startDate));

            loss = 1. - recoveryRate;

            for (int i = 1; i < tl.fNumItems; ++i) {
                double lambda;
                double fwdRate;
                double thisPv = 0;
                double lambdafwdRate;

                s0 = s1;
                df0 = df1;
                s1 = cdsForwardZeroPrice(spreadCurve, today, tl.dateArray[i]);
                df1 = cdsForwardZeroPrice(discountCurve, today, tl.dateArray[i]);
                t = (double) (tl.dateArray[i - 1].periodUntil(tl.dateArray[i], ChronoUnit.DAYS)) / 365.;

                /*************************Markit Proposed Fix***************************************************
                 *
                 * Some of the division of original ISDA model can be removed
                 * lambda  = log(s0 / s1) / t = (log(s0) - log(s1)) / t
                 * fwdRate = log(df0 / df) / t = (log(df0) - log(df1)) / t
                 * Divisions by t can be absorbed by later formulas as well.
                 */
                lambda = Math.log(s0) - Math.log(s1);
                fwdRate = Math.log(df0) - Math.log(df1);
                lambdafwdRate = lambda + fwdRate + 1.0e-50;

                /**
                 * If lambdafwdRate is extremely small, original calculation generates big noise on computer
                 * due to the small denominator.
                 * In this case, Talyor expansion is employed to remove lambdafwdRate from denomintor
                 * so that numerical noise is signicantly reduced.
                 */
                if (Math.abs(lambdafwdRate) > 1.e-4) {

                    thisPv = loss * lambda / lambdafwdRate * (1.0 - Math.exp(-lambdafwdRate)) * s0 * df0;
                } else {
                    final double thisPv0 = loss * lambda * s0 * df0;
                    final double thisPv1 = -thisPv0 * lambdafwdRate * .5;
                    final double thisPv2 = -thisPv1 * lambdafwdRate / 3.;
                    final double thisPv3 = -thisPv2 * lambdafwdRate * .25;
                    final double thisPv4 = -thisPv3 * lambdafwdRate * .2;

                    thisPv += thisPv0;
                    thisPv += thisPv1;
                    thisPv += thisPv2;
                    thisPv += thisPv3;
                    thisPv += thisPv4;
                }

                myPv += thisPv;
            }
        }

        pv.set(myPv);
        return ReturnStatus.SUCCESS;
    }

    /**
     * **************************************************************************
     * * Computes a one period integral with payment at a specific payment date.
     * * This is actually trivial.
     * **************************************************************************
     */
    private ReturnStatus onePeriodIntegralAtPayDate(LocalDate today,
                                                    LocalDate startDate,
                                                    LocalDate endDate,
                                                    LocalDate payDate,
                                                    TCurve discountCurve,
                                                    TCurve spreadCurve,
                                                    double recoveryRate,
                                                    DoubleHolder pv) {

        double df, s0, s1, loss;

        assert (endDate.isAfter(startDate));
        assert (discountCurve != null);
        assert (spreadCurve != null);
        assert (pv != null);

        if (today.isAfter(endDate)) {
            pv.set(0);

        } else {
            s0 = cdsForwardZeroPrice(spreadCurve, today, startDate);
            s1 = cdsForwardZeroPrice(spreadCurve, today, endDate);
            df = cdsForwardZeroPrice(discountCurve, today, payDate);
            loss = 1. - recoveryRate;
            pv.set((s0 - s1) * df * loss);
        }

        return ReturnStatus.SUCCESS;

    }

    public LocalDate max(LocalDate d1, LocalDate d2) {
        return d1.isAfter(d2) ? d1 : d2;
    }
}
