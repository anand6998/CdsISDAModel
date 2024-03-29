package com.anand.analytics.isdamodel.cds;


import com.anand.analytics.isdamodel.date.Day;
import com.anand.analytics.isdamodel.domain.CdsSpreadContext;
import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import com.anand.analytics.isdamodel.domain.TContingentLeg;
import com.anand.analytics.isdamodel.domain.TCurve;
import com.anand.analytics.isdamodel.domain.TDateInterval;
import com.anand.analytics.isdamodel.domain.TFeeLeg;
import com.anand.analytics.isdamodel.domain.TProtPayConv;
import com.anand.analytics.isdamodel.domain.TStubMethod;
import com.anand.analytics.isdamodel.utils.DayCount;
import com.anand.analytics.isdamodel.utils.DoubleHolder;
import com.anand.analytics.isdamodel.utils.ExcelFunctions;
import com.anand.analytics.isdamodel.utils.ReturnStatus;
import com.anand.analytics.isdamodel.utils.RootFindBrent;
import com.anand.analytics.isdamodel.utils.SolvableFunction;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Created by Anand on 10/21/2014.
 */

class CdsoneSpreadSolverFunction implements SolvableFunction {
    final static Logger logger = Logger.getLogger(CdsoneSpreadSolverFunction.class);
    public ReturnStatus eval(double onespread,
                             Object data,
                             DoubleHolder diff) {
        DoubleHolder upfrontCharge = new DoubleHolder();
        CdsSpreadContext context = (CdsSpreadContext) data;
        if (CdsOne.cdsCdsoneUpfrontCharge(context.today,
                context.valueDate,
                context.benchmarkStartDate,
                context.stepinDate,
                context.startDate,
                context.endDate,
                context.couponRate,
                context.payAccruedOnDefault,
                context.dateInterval,
                context.stubType,
                context.accrueDCC,
                context.badDayConv,
                context.calendar,
                context.discCurve,
                onespread,
                context.recoveryRate,
                context.payAccruedAtStart,
                upfrontCharge).equals(ReturnStatus.FAILURE)) {
            logger.error("CdsoneSpreadSolverFunction.eval()::Error calculating cdsOneUpfrontCharge");
            return ReturnStatus.FAILURE;
        };

        diff.set(upfrontCharge.get() - context.upfrontCharge);
        return ReturnStatus.SUCCESS;
    }
}

public class CdsOne {
    /**
     * Computes the flat spread required to match the upfront charge
     */
    final static Logger logger = Logger.getLogger(CdsOne.class);
    public static ReturnStatus cdsCdsoneSpread(Day today,
                                               Day valueDate,
                                               Day benchmarkStartDate,
                                               Day stepinDate,
                                               Day startDate,
                                               Day endDate,
                                               double couponRate,
                                               boolean payAccruedOnDefault,
                                               TDateInterval dateInterval,
                                               TStubMethod stubType,
                                               DayCount accruedDcc,
                                               TBadDayConvention badDayConvention,
                                               String calendar,
                                               TCurve discountCurve,
                                               double upfrontCharge,
                                               double recoveryRate,
                                               boolean payAccruedAtStart,
                                               DoubleHolder oneSpread) {
        CdsSpreadContext context = new CdsSpreadContext();
        context.today = today;
        context.valueDate = valueDate;
        context.benchmarkStartDate = benchmarkStartDate;
        context.stepinDate = stepinDate;
        context.startDate = startDate;
        context.endDate = endDate;
        context.couponRate = couponRate;

        context.payAccruedOnDefault = payAccruedOnDefault;
        context.dateInterval = dateInterval;
        context.stubType = stubType;
        context.accrueDCC = accruedDcc;
        context.badDayConv = badDayConvention;
        context.calendar = calendar;
        context.discCurve = discountCurve;
        context.upfrontCharge = upfrontCharge;
        context.recoveryRate = recoveryRate;
        context.payAccruedAtStart = payAccruedAtStart;

        if (RootFindBrent.findRoot(new CdsoneSpreadSolverFunction(), context,
                0.0,
                100.0,
                100,
                0.01,
                0.0001,
                0.0,
                1e-8,
                1e-8,
                oneSpread).equals(ReturnStatus.FAILURE)) {
            logger.error("CdsOne.cdsCdsoneSpread():: Error finding root");
            return ReturnStatus.FAILURE;
        }

        return ReturnStatus.SUCCESS;


    }

    public static ReturnStatus cdsCdsParSpreads(
        Day today,
        Day stepinDate,
        Day startDate,
        Day[] endDates,
        boolean payAccOnDefault,
        TDateInterval couponInterval,
        TStubMethod stubType,
        DayCount paymentDcc,
        TBadDayConvention badDayConvention,
        String calendar,
        TCurve discountCurve,
        TCurve spreadCurve,
        double recoveryRate,
        DoubleHolder[] parSpread
    ) {
        try {
            ReturnStatus status = ReturnStatus.FAILURE;

            boolean isPriceClean = true;
            boolean protectStart = true;

            Validate.notNull(parSpread, "parSpread is null");
            Validate.isTrue(endDates.length >= 1, "endDates.length < 1");
            Validate.isTrue(stepinDate.isAfter(today) || stepinDate.isEqual(today), "stepinDate < today");

            for (int i = 0; i < endDates.length; i++) {
                DoubleHolder feeLegPV = new DoubleHolder();
                DoubleHolder contingentLegPV = new DoubleHolder();
                if (cdsCdsFeeLegPV(today,
                        stepinDate,
                        stepinDate,
                        startDate,
                        endDates[i],
                        payAccOnDefault,
                        couponInterval,
                        stubType,
                        1.0, /*Notional*/
                        1.0, /*Coupon Rate */
                        paymentDcc,
                        badDayConvention,
                        calendar,
                        discountCurve,
                        spreadCurve,
                        protectStart,
                        isPriceClean,
                        feeLegPV).equals(ReturnStatus.FAILURE)) {
                    logger.error("CdsOne.cdsCdsParSpreads::Error in cdsCdsFeeLegPV");
                    return ReturnStatus.FAILURE;
                }

                if (cdsCdsContingentLegPV(today,
                        stepinDate,
                        ExcelFunctions.MAX_DATE(stepinDate, startDate),
                        endDates[i],
                        1.0, /*Notional*/
                        discountCurve,
                        spreadCurve,
                        recoveryRate,
                        protectStart,
                        contingentLegPV).equals(ReturnStatus.FAILURE)) {
                    logger.error("CdsOne.cdsCdsParSpreads::Error in cdsCdsContingentLegPV");
                    return ReturnStatus.FAILURE;
                }

                parSpread[i].set(contingentLegPV.get() / feeLegPV.get());
            }

            return ReturnStatus.SUCCESS;
        } catch (Exception ex) {
            logger.error(ex);
            return ReturnStatus.FAILURE;
        }
    }

    public static ReturnStatus cdsCdsoneUpfrontCharge(
            Day today,
            Day valueDate,
            Day benchmarkStartDate,
            Day stepinDate,
            Day startDate,
            Day endDate,
            double couponRate,
            boolean payAccruedOnDefault,
            TDateInterval dateInterval,
            TStubMethod stubType,
            DayCount accruedDCC,
            TBadDayConvention badDayConv,
            String calendar,
            TCurve discCurve,
            double oneSpread,
            double recoveryRate,
            boolean payAccruedAtStart,
            DoubleHolder upfrontCharge
    ) {

        String routine = "CdsoneUpfrontCharge";
        ReturnStatus status = ReturnStatus.FAILURE;

        final TCurve flatSpreadCurve;

        final Day endDates[] = {endDate};
        final double[] couponRates = {oneSpread};

        try {

            flatSpreadCurve = CdsBootstrap.cdsCleanSpreadCurve(
                    today,
                    discCurve,
                    benchmarkStartDate,
                    stepinDate,
                    valueDate,
                    1,
                    endDates,
                    couponRates,
                    null,
                    recoveryRate,
                    payAccruedOnDefault,
                    dateInterval,
                    accruedDCC,
                    stubType,
                    badDayConv,
                    calendar);

            if (cdsCdsPrice(today,
                    valueDate,
                    stepinDate,
                    startDate,
                    endDate,
                    couponRate,
                    payAccruedOnDefault,
                    dateInterval,
                    stubType,
                    accruedDCC,
                    badDayConv,
                    calendar,
                    discCurve,
                    flatSpreadCurve,
                    recoveryRate,
                    payAccruedAtStart,
                    upfrontCharge).equals(ReturnStatus.FAILURE)) {
                throw new Exception("CdsOne.cdsCdsoneUpfrontCharge()::Error calculating Cds price");
            }
        } catch (Exception ex) {
            logger.error(ex);
            return ReturnStatus.FAILURE;
        }

        return ReturnStatus.SUCCESS;
    }

    public static ReturnStatus cdsCdsPrice(Day today,
                                            Day settleDate,
                                            Day stepinDate,
                                            Day startDate,
                                            Day endDate,
                                            double couponRate,
                                            boolean payAccruedOnDefault,
                                            TDateInterval dateInterval,
                                            TStubMethod stubType,
                                            DayCount paymentDcc,
                                            TBadDayConvention badDayConvention,
                                            String calendar,
                                            TCurve discCurve,
                                            TCurve spreadCurve,
                                            double recoveryRate,
                                            boolean isPriceClean,
                                            DoubleHolder price) {

        final DoubleHolder feeLegPV = new DoubleHolder();
        final DoubleHolder contingentLegPV = new DoubleHolder();

        boolean protectStart = true;

        Validate.notNull (price, "price is null");
        Validate.isTrue (stepinDate.isAfter(today) || stepinDate.isEqual(today), "stepinDate < today");

        final Day valueDate = settleDate;
        if (cdsCdsFeeLegPV(today,
                valueDate,
                stepinDate,
                startDate,
                endDate,
                payAccruedOnDefault,
                dateInterval,
                stubType,
                1.0, /* Notional */
                couponRate,
                paymentDcc,
                badDayConvention,
                calendar,
                discCurve,
                spreadCurve,
                protectStart,
                isPriceClean,
                feeLegPV).equals(ReturnStatus.FAILURE)) {
            logger.error("CdsOne.cdsCdsPrice()::Error calculating fee leg PV");
            return ReturnStatus.FAILURE;
        }

        final Day maxDate = stepinDate.isAfter(startDate) ? stepinDate : startDate;
        if (maxDate.isEqual(endDate) || maxDate.isBefore(endDate)) {
            if (cdsCdsContingentLegPV(today,
                    valueDate,
                    maxDate,
                    endDate,
                    1.0, /* Notional */
                    discCurve,
                    spreadCurve,
                    recoveryRate,
                    protectStart,
                    contingentLegPV).equals(ReturnStatus.FAILURE)) {
                logger.error("CdsOne.cdsCdsPrice()::Error calculating contingent leg PV");
                return ReturnStatus.FAILURE;
            }
        }

        price.set(contingentLegPV.get() - feeLegPV.get());
        return ReturnStatus.SUCCESS;
    }

    private static ReturnStatus cdsCdsContingentLegPV(Day today,
                                                      Day valueDate,
                                                      Day startDate,
                                                      Day endDate,
                                                      double notional,
                                                      TCurve discCurve,
                                                      TCurve spreadCurve,
                                                      double recoveryRate,
                                                      boolean protectStart,
                                                      DoubleHolder pv) {
        try {
            TContingentLeg cl = new TContingentLeg(startDate, endDate, notional, protectStart, TProtPayConv.PROT_PAY_DEF);
            if (cl.getPV(today, valueDate, startDate, discCurve, spreadCurve, recoveryRate, pv).equals(ReturnStatus.FAILURE)) {
                logger.error("CdsOne.cdsCdsContingentLegPV()::Error calculating ContingentLeg PV");
                return ReturnStatus.FAILURE;
            }

            return ReturnStatus.SUCCESS;
        } catch (Exception ex) {
            logger.error(ex);
            return ReturnStatus.FAILURE;
        }
    }

    private static ReturnStatus cdsCdsFeeLegPV(Day today,
                                               Day valueDate,
                                               Day stepinDate,
                                               Day startDate,
                                               Day endDate,
                                               boolean payAccruedOnDefault,
                                               TDateInterval dateInterval,
                                               TStubMethod stubType,
                                               double notional,
                                               double couponRate,
                                               DayCount paymentDcc,
                                               TBadDayConvention badDayConvention,
                                               String calendar,
                                               TCurve discCurve,
                                               TCurve spreadCurve,
                                               boolean protectStart,
                                               boolean cleanPrice,
                                               DoubleHolder pv) {
        try {
            final TFeeLeg fl = new TFeeLeg(startDate, endDate, payAccruedOnDefault, dateInterval,
                    stubType, notional, couponRate, paymentDcc, badDayConvention,
                    calendar, protectStart);

            if (fl.getPV(today, valueDate, stepinDate, discCurve, spreadCurve, cleanPrice, pv).equals(ReturnStatus.FAILURE)) {
                logger.error("CdsOne.cdsCdsFeeLegPV()::Error calculating fee leg PV");
                return ReturnStatus.FAILURE;
            }
            return ReturnStatus.SUCCESS;
        } catch(Exception ex) {
            logger.error(ex);
            return ReturnStatus.FAILURE;
        }
    }
}
