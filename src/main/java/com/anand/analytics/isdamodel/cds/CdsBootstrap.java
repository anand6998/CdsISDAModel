package com.anand.analytics.isdamodel.cds;


import com.anand.analytics.isdamodel.date.Day;
import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import com.anand.analytics.isdamodel.domain.TContingentLeg;
import com.anand.analytics.isdamodel.domain.TCurve;
import com.anand.analytics.isdamodel.domain.TDateInterval;
import com.anand.analytics.isdamodel.domain.TFeeLeg;
import com.anand.analytics.isdamodel.domain.TProtPayConv;
import com.anand.analytics.isdamodel.domain.TStubMethod;
import com.anand.analytics.isdamodel.utils.CdsBootstrapContext;
import com.anand.analytics.isdamodel.utils.DayCount;
import com.anand.analytics.isdamodel.utils.DayCountBasis;
import com.anand.analytics.isdamodel.utils.DoubleHolder;
import com.anand.analytics.isdamodel.utils.PeriodType;
import com.anand.analytics.isdamodel.utils.ReturnStatus;
import com.anand.analytics.isdamodel.utils.RootFindBrent;
import com.anand.analytics.isdamodel.utils.SolvableFunction;
import org.apache.log4j.Logger;

import static com.anand.analytics.isdamodel.domain.TRateFunctions.cdsConvertCompoundRate;
import static com.anand.analytics.isdamodel.domain.TRateFunctions.cdsForwardZeroPrice;

/**
 * Created by Anand on 10/21/2014.
 */
public class CdsBootstrap {
    final static Logger logger = Logger.getLogger(CdsBootstrap.class);

    /**
     * The main bootstrap routine
     */
    public static TCurve cdsCleanSpreadCurve
    (Day today,           /* (I) Used as credit curve base date       */
     TCurve discountCurve,      /* (I) Risk-free discount curve             */
     Day startDate,       /* (I) Start of CDS for accrual and risk    */
     Day stepinDate,      /* (I) Stepin date                          */
     Day cashSettleDate,  /* (I) Pay date                             */
     long nbDate,               /* (I) Number of benchmark dates            */
     Day[] endDates,      /* (I) Maturity dates of CDS to bootstrap   */
     double[] couponRates,      /* (I) CouponRates (e.g. 0.05 = 5% = 500bp) */
     Boolean[] includes,        /* (I) Include this date. Can be NULL if
                                        all are included.                           */
     double recoveryRate,       /* (I) Recovery rate                        */
     boolean payAccOnDefault,   /* (I) Pay accrued on default               */
     TDateInterval couponInterval,  /* (I) Interval between fee payments    */
     DayCount paymentDCC,       /* (I) DCC for fee payments and accrual     */
     TStubMethod stubType,      /* (I) Stub type for fee leg                */
     TBadDayConvention badDayConv,
     String calendar
    ) throws Exception {


        Day[] includeEndDates = null;
        double[] includeCouponRates = null;

        final TDateInterval ivl3M = new TDateInterval(3, PeriodType.M, 0);
        if (couponInterval == null)
            couponInterval = ivl3M;

        if (includes != null) {
            /* need to pick and choose which names appear */
            int i;
            int nbInclude = 0;
            int j;
            for (i = 0; i < nbDate; ++i) {
                if (includes[i])
                    ++nbInclude;
            }
            if (nbInclude == 0) {
                throw new RuntimeException("Something wrong with includes");
            }

            includeEndDates = new Day[nbInclude];
            includeCouponRates = new double[nbInclude];

            j = 0;
            for (i = 0; i < nbDate; ++i) {
                if (includes[i]) {
                    includeEndDates[j] = endDates[i];
                    includeCouponRates[j] = couponRates[i];
                    ++j;
                }
            }

            if (j == nbInclude) {
                //good
            } else
                throw new RuntimeException("Something wrong with includes");

            nbDate = nbInclude;
            endDates = includeEndDates;
            couponRates = includeCouponRates;
        }

        final TCurve out = CdsBootstrap.bootstrap(today,
                discountCurve,
                startDate,
                stepinDate,
                cashSettleDate,
                nbDate,
                endDates,
                couponRates,
                recoveryRate,
                payAccOnDefault,
                couponInterval,
                paymentDCC,
                stubType,
                badDayConv,
                calendar);
        return out;
    }

    /**
     * **************************************************************************
     * * This is the CDS bootstrap routine.
     * *
     * * Very little attempt has been made at extreme optimisation - this is quite
     * * a basic bootstrap routine which simply calls the underlying CDS pricer
     * * for each benchmark instrument while it changes the CDS zero rate at the
     * * maturity date of the benchmark instrument.
     * *
     * **************************************************************************
     */
    public static TCurve bootstrap(Day today,                /* Used as credit curve base date       */
                                   TCurve discountCurve,           /* Risk Free discount curve             */
                                   Day startDate,            /* Start of CDS for accrual and risk    */
                                   Day stepinDate,           /* Stepin date                          */
                                   Day cashSettleDate,       /* Pay date                             */
                                   long nbDate,                    /* Number of benchmark dates            */
                                   Day[] endDates,           /* Maturity dates of CDS to bootstrap   */
                                   double[] couponRates,           /* Coupon rates e.g. 0.05 = 5% = 500 bp */
                                   double recoveryRate,            /* Recovery rate                        */
                                   boolean payAccOnDefault,        /* Pay accrued on default               */
                                   TDateInterval couponInterval,   /* Interval between fee payments        */
                                   DayCount paymentDCC,            /* DCC for fee payments and accrual     */
                                   TStubMethod stubType,           /* Stub type for fee leg                */
                                   TBadDayConvention badDayConv,
                                   String calendar) throws Exception {


        final boolean protectStart = true;


        final TCurve cdsCurve = new TCurve(today, endDates, couponRates, DayCountBasis.CONTINUOUS_BASIS, DayCount.ACT_365F);
        final CdsBootstrapContext context = new CdsBootstrapContext();
        context.discCurve = discountCurve;
        context.cdsCurve = cdsCurve;
        context.recoveryRate = recoveryRate;
        context.stepinDate = stepinDate;
        context.cashSettleDate = cashSettleDate;

        for (int i = 0; i < endDates.length; i++) {

            final DoubleHolder spread = new DoubleHolder();
            final double guess = couponRates[i] / (1.0 - recoveryRate);
            final Day maxDate = startDate.isAfter(today) ? startDate : today;
            final TContingentLeg cl = new TContingentLeg(maxDate, endDates[i], 1.0, protectStart, TProtPayConv.PROT_PAY_DEF);
            final TFeeLeg fl = new TFeeLeg(startDate,
                    endDates[i],
                    payAccOnDefault,
                    couponInterval,
                    stubType,
                    1.0,
                    couponRates[i],
                    paymentDCC,
                    badDayConv,
                    calendar,
                    protectStart);

            context.i = i;
            context.contigentLeg = cl;
            context.feeLeg = fl;

            final SolvableFunction function = new CdsBootStrapFunction();
            if (RootFindBrent.findRoot(function,
                    context, /* data */
                    0.0,    /* boundLo */
                    1e10,   /* boundHi */
                    100,    /* numIterations */
                    guess,
                    0.0005, /* initialXStep */
                    0,      /* initialFDeriv */
                    1e-10,  /* xacc */
                    1e-10,  /* facc */
                    spread
            ).equals(ReturnStatus.FAILURE)) {
                logger.error("CdsBootstrap.bootstrap()::Error finding root");
                throw new Exception("CdsBootstrap.bootstrap()::Error finding root");
            }

            cdsCurve.getRates()[i] = spread.get();

            /** check if forward hazard rate is negative */
            if (i > 0) {
                double fwdPrice = cdsForwardZeroPrice(cdsCurve, endDates[i - 1], endDates[i]);
                if (fwdPrice > 1) {
                    throw new RuntimeException("Negative forward hazard rate at maturity " + endDates[i]);
                }

            }
        }

        creditCurveConvertRateType(cdsCurve, DayCountBasis.ANNUAL_BASIS);
        return cdsCurve;
    }

    private static void creditCurveConvertRateType(TCurve curve, DayCountBasis dayCountBasis) {
        if (dayCountBasis.equals(curve.getBasis())) {
            return;
        } else {
            for (int i = 0; i < curve.getDates().length; i++) {
                DoubleHolder convertedRate = new DoubleHolder();
                cdsConvertCompoundRate(curve.getRates()[i],
                        curve.getBasis(),
                        curve.getDayCountConv(),
                        dayCountBasis,
                        curve.getDayCountConv(),
                        convertedRate);
                curve.getRates()[i] = convertedRate.get();

            }

            curve.setBasis(dayCountBasis);
        }
    }
}

class CdsBootStrapFunction implements SolvableFunction {
    final static Logger logger = Logger.getLogger(CdsBootStrapFunction.class);

    public ReturnStatus eval(double cleanSpread, Object data, DoubleHolder y) {
        final CdsBootstrapContext context = (CdsBootstrapContext) data;

        final int i = context.i;
        final TCurve discountCurve = context.discCurve;
        final TCurve cdsCurve = context.cdsCurve;
        final double recoveryRate = context.recoveryRate;
        final TContingentLeg cl = context.contigentLeg;
        final TFeeLeg fl = context.feeLeg;
        final Day cdsBaseDate = cdsCurve.getBaseDate();
        final Day stepInDate = context.stepinDate;
        final Day cashSettleDate = context.cashSettleDate;
        final boolean isPriceClean = true;

        final double pvC; /* PV of contingent leg */
        final double pvF; /* PV of fee leg */

        cdsCurve.getRates()[i] = cleanSpread;

        DoubleHolder result = new DoubleHolder();

        ReturnStatus status = cl.getPV(cdsBaseDate, cashSettleDate, stepInDate, discountCurve, cdsCurve, recoveryRate, result);
        if (status.equals(ReturnStatus.FAILURE)) {
            logger.error("CdsBootStrapFunction.eval()::Error calculating contingent leg PV");
            return ReturnStatus.FAILURE;
        }

        pvC = result.get();

        result = new DoubleHolder(0);

        status = fl.getPV(cdsBaseDate, cashSettleDate, stepInDate, discountCurve, cdsCurve, isPriceClean, result);
        if (status.equals(ReturnStatus.FAILURE)) {
            logger.error("CdsBootStrapFunction.eval()::Error calculating fee leg PV");
            return ReturnStatus.FAILURE;
        }

        pvF = result.get();

        y.set(pvC - pvF);
        return ReturnStatus.SUCCESS;

    }
}