package com.anand.analytics.isdamodel.server;


import com.anand.analytics.isdamodel.cds.*;
import com.anand.analytics.isdamodel.context.CdsCacheManager;
import com.anand.analytics.isdamodel.context.XlServerSpringUtils;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.utils.*;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.boris.xlloop.reflect.XLFunction;
import org.boris.xlloop.xloper.XLArray;
import org.boris.xlloop.xloper.XLNum;
import org.boris.xlloop.xloper.XLString;
import org.boris.xlloop.xloper.XLoper;
import org.threeten.bp.LocalDate;

import java.util.UUID;

import static com.anand.analytics.isdamodel.cds.TDateFunctions.cdsDayCountFraction;

/**
 * Created by aanand on 12/3/2014.
 */
public class CdsFunctionLibrary {
    private final static Logger logger = Logger.getLogger(CdsFunctionLibrary.class);
    private final static CdsCacheManager cdsCacheManager = (CdsCacheManager) XlServerSpringUtils.getBeanByName("cdsCacheManager");

    @XLFunction(name = "APO.CDS.IRZeroCurveMake", args = {
            "BaseDate",
            "Dates",
            "Rates",
            "Basis",
            "DCC"

    }, argHelp = {
            "BaseDate",
            "Dates",
            "Rates",
            "Basis",
            "DCC"

    }, category = "APO CDS Functions")
    public static XLoper cdsIrZeroCurveMake(Double xlBaseDate,
                                            double[] xlDates,
                                            double[] rates,
                                            Integer xlBasis,
                                            String dcc) {
        try {
            Validate.notNull(xlBaseDate, "Invalid Base date");
            Validate.notNull(xlDates, "Invalid dates array");
            Validate.notNull(rates, "Invalid rates array");
            Validate.notNull(xlBasis, "Invalid basis");

            Validate.isTrue(!(Double.compare(xlBaseDate, 0) == 0.0));
            for (int i = 0; i < xlDates.length; i++)
                Validate.isTrue(!(Double.compare(xlDates[i], 0) == 0.0));

            final LocalDate baseDate = ExcelFunctions.xlDateToLocalDateTime(xlBaseDate);
            final LocalDate[] dates = ExcelFunctions.xlDatesToLocalDateTimeArray(xlDates);
            final DayCountBasis dayCountBasis = ExcelFunctions.xlIntToDayCountBasis(xlBasis);

            final DayCount dayCount = dcc == null ? DayCount.ACT_360 : ExcelFunctions.cdsStringToDayCountConv(dcc);
            final TCurve tCurve = new TCurve(baseDate, dates, rates, dayCountBasis, dayCount);

            final String tCurveKey = "TCurve@" + UUID.randomUUID().toString();

            cdsCacheManager.put(tCurveKey, tCurve);
            return new XLString(tCurveKey);
        } catch (Exception ex) {
            logger.error(ex);
            return new XLString(ex.getMessage());
        }
    }


    @XLFunction(name = "APO.CDS.ParSpreadFlat", args = {
            "Today",
            "ValueDate",
            "BenchmarkStartDate",
            "StepinDate",
            "StartDate",
            "EndDate",
            "CouponRate",
            "PayAccruedOnDefault",
            "CouponInterval",
            "StubType",
            "PaymentDcc",
            "BadDayConvention",
            "Holidays",
            "DiscountCurve",
            "UpfrontCharge",
            "RecoveryRate",
            "IsPriceClean"
    },
            argHelp = {
                    "Today",
                    "ValueDate",
                    "BenchmarkStartDate",
                    "StepinDate",
                    "StartDate",
                    "EndDate",
                    "CouponRate",
                    "PayAccruedOnDefault",
                    "CouponInterval",
                    "StubType",
                    "PaymentDcc",
                    "BadDayConvention",
                    "Holidays",
                    "DiscountCurve",
                    "UpfrontCharge",
                    "RecoveryRate",
                    "IsPriceClean"
            }, category = "APO CDS Functions")
    public static XLoper cdsParSpreadFlat(Double xldToday,
                                          Double xldValueDate,
                                          Double xldBenchmarktStartDate,
                                          Double xldstepinDate,
                                          Double xldStartDate,
                                          Double xldEndDate,
                                          Double xldCouponRate,
                                          Integer xliPayAccruedOnDefault,
                                          String xlsCouponInterval,
                                          String xlsStubType,
                                          String xlsPaymentDcc,
                                          String xlsBadDayConvention,
                                          String xlsHolidays,
                                          String xlhDiscountCurveKey,
                                          Double xldUpfrontCharge,
                                          Double xldRecoveryRate,
                                          Integer xliIsPriceClean
    ) {
        try {
            Validate.notNull(xldToday, "Invalid todayDate");
            Validate.notNull(xldValueDate, "Invalid valueDate");
            Validate.notNull(xldBenchmarktStartDate, "Invalid benchmarkStartDate");
            Validate.notNull(xldstepinDate, "Invalid stepinDate");
            Validate.notNull(xldStartDate, "Invalid startDate");
            Validate.notNull(xldEndDate, "Invalid endDate");
            Validate.notNull(xldCouponRate, "Invalid couponRate");
            Validate.notNull(xliPayAccruedOnDefault, "Invalid payAccruedOnDefault");
            Validate.notNull(xlsCouponInterval, "Invalid couponInterval");
            Validate.notNull(xlhDiscountCurveKey, "Invalid discount curve handle");
            Validate.notNull(xldUpfrontCharge, "Invalid upfront charge");
            Validate.notNull(xldRecoveryRate, "Invalid recoveryRate");
            Validate.notNull(xliIsPriceClean, "Invalid isPriceClean");

            Validate.isTrue(!(Double.compare(xldToday, 0) == 0.0), "Invalid todayDate");
            Validate.isTrue(!(Double.compare(xldValueDate, 0) == 0.0), "Invalid valueDate");
            Validate.isTrue(!(Double.compare(xldBenchmarktStartDate, 0) == 0.0), "Invalid benchmarkStartDate");
            Validate.isTrue(!(Double.compare(xldstepinDate, 0) == 0.0), "Invalid stepinDate");
            Validate.isTrue(!(Double.compare(xldStartDate, 0) == 0.0), "Invalid startDate");
            Validate.isTrue(!(Double.compare(xldEndDate, 0) == 0.0), "Invalid endDate");

            final LocalDate today = ExcelFunctions.xlDateToLocalDateTime(xldToday);
            final LocalDate valueDate = ExcelFunctions.xlDateToLocalDateTime(xldValueDate);
            final LocalDate benchmarkStartDate = ExcelFunctions.xlDateToLocalDateTime(xldBenchmarktStartDate);
            final LocalDate stepinDate = ExcelFunctions.xlDateToLocalDateTime(xldstepinDate);
            final LocalDate startDate = ExcelFunctions.xlDateToLocalDateTime(xldStartDate);
            final LocalDate endDate = ExcelFunctions.xlDateToLocalDateTime(xldEndDate);
            final double couponRate = xldCouponRate;
            final boolean payAccruedOnDefault = xliPayAccruedOnDefault == 1 ? true : false;
            final TDateInterval ivl = ExcelFunctions.cdsStringToDateInterval(xlsCouponInterval);

            final TStubMethod tStubMethod = xlsStubType == null
                    ? ExcelFunctions.DEFAULT_STUB_METHOD
                    : ExcelFunctions.cdsStringToStubMethod(xlsStubType);

            final DayCount paymentDcc = xlsPaymentDcc == null
                    ? ExcelFunctions.DEFAULT_DAY_COUNT
                    : ExcelFunctions.cdsStringToDayCountConv(xlsPaymentDcc);

            final TBadDayConvention badDayConvention = xlsBadDayConvention == null
                    ? ExcelFunctions.DEFAULT_BAD_DAY_CONVENTION
                    : ExcelFunctions.cdsStringToBadDayConv(xlsBadDayConvention);

            final String holidays = xlsHolidays == null
                    ? ExcelFunctions.DEFAULT_HOLIDAY_CALENDAR
                    : new String(xlsHolidays);

            final TCurve discountCurve = cdsCacheManager.get(xlhDiscountCurveKey);
            final double upfrontCharge = xldUpfrontCharge;
            final double recoveryRate = xldRecoveryRate;
            final boolean isPriceClean = xliIsPriceClean == 1 ? true : false;

            DoubleHolder result = new DoubleHolder();
            if (CdsOne.cdsCdsoneSpread(today,
                    valueDate,
                    benchmarkStartDate,
                    stepinDate,
                    startDate,
                    endDate,
                    couponRate,
                    payAccruedOnDefault,
                    ivl,
                    tStubMethod,
                    paymentDcc,
                    badDayConvention,
                    holidays,
                    discountCurve,
                    upfrontCharge,
                    recoveryRate,
                    isPriceClean,
                    result) == ReturnStatus.FAILURE) {
                throw new CdsLibraryException("CdsFunctionLibrary.cdsParSpreadFlat()::Error calculating CdsOne.cdsCdsOneSpread");
            }

            return new XLNum(result.get());
        } catch (Exception ex) {
            logger.error(ex);
            return new XLString(ex.getMessage());
        }
    }


    @XLFunction(name = "APO.CDS.UpfrontFlat", args = {
            "Today",
            "ValueDate",
            "BenchmarkStartDate",
            "StepinDate",
            "StartDate",
            "EndDate",
            "CouponRate",
            "PayAccruedOnDefault",
            "CouponInterval",
            "StubType",
            "PaymentDcc",
            "BadDayConvention",
            "Holidays",
            "DiscountCurve",
            "ParSpread",
            "RecoveryRate",
            "IsPriceClean"
    },
            argHelp = {
                    "Today",
                    "ValueDate",
                    "BenchmarkStartDate",
                    "StepinDate",
                    "StartDate",
                    "EndDate",
                    "CouponRate",
                    "PayAccruedOnDefault",
                    "CouponInterval",
                    "StubType",
                    "PaymentDcc",
                    "BadDayConvention",
                    "Holidays",
                    "DiscountCurve",
                    "ParSpread",
                    "RecoveryRate",
                    "IsPriceClean"
            }, category = "APO CDS Functions")
    public static XLoper cdsUpfrontFlat(Double xldToday,
                                        Double xldValueDate,
                                        Double xldBenchmarktStartDate,
                                        Double xldstepinDate,
                                        Double xldStartDate,
                                        Double xldEndDate,
                                        Double xldCouponRate,
                                        Integer xliPayAccruedOnDefault,
                                        String xlsCouponInterval,
                                        String xlsStubType,
                                        String xlsPaymentDcc,
                                        String xlsBadDayConvention,
                                        String xlsHolidays,
                                        String xlhDiscountCurveKey,
                                        Double xldParSpread,
                                        Double xldRecoveryRate,
                                        Integer xliIsPriceClean
    ) {
        try {
            Validate.notNull(xldToday, "Invalid todayDate");
            Validate.notNull(xldValueDate, "Invalid valueDate");
            Validate.notNull(xldBenchmarktStartDate, "Invalid benchmarkStartDate");
            Validate.notNull(xldstepinDate, "Invalid stepinDate");
            Validate.notNull(xldStartDate, "Invalid startDate");
            Validate.notNull(xldEndDate, "Invalid endDate");
            Validate.notNull(xldCouponRate, "Invalid couponRate");
            Validate.notNull(xliPayAccruedOnDefault, "Invalid payAccruedOnDefault");
            Validate.notNull(xlsCouponInterval, "Invalid couponInterval");
            Validate.notNull(xlhDiscountCurveKey, "Invalid discount curve handle");
            Validate.notNull(xldParSpread, "Invalid parSpread");
            Validate.notNull(xldRecoveryRate, "Invalid recoveryRate");
            Validate.notNull(xliIsPriceClean, "Invalid isPriceClean");

            Validate.isTrue(!(Double.compare(xldToday, 0) == 0.0), "Invalid todayDate");
            Validate.isTrue(!(Double.compare(xldValueDate, 0) == 0.0), "Invalid valueDate");
            Validate.isTrue(!(Double.compare(xldBenchmarktStartDate, 0) == 0.0), "Invalid benchmarkStartDate");
            Validate.isTrue(!(Double.compare(xldstepinDate, 0) == 0.0), "Invalid stepinDate");
            Validate.isTrue(!(Double.compare(xldStartDate, 0) == 0.0), "Invalid startDate");
            Validate.isTrue(!(Double.compare(xldEndDate, 0) == 0.0), "Invalid endDate");

            final LocalDate today = ExcelFunctions.xlDateToLocalDateTime(xldToday);
            final LocalDate valueDate = ExcelFunctions.xlDateToLocalDateTime(xldValueDate);
            final LocalDate benchmarkStartDate = ExcelFunctions.xlDateToLocalDateTime(xldBenchmarktStartDate);
            final LocalDate stepinDate = ExcelFunctions.xlDateToLocalDateTime(xldstepinDate);
            final LocalDate startDate = ExcelFunctions.xlDateToLocalDateTime(xldStartDate);
            final LocalDate endDate = ExcelFunctions.xlDateToLocalDateTime(xldEndDate);

            final double couponRate = xldCouponRate;
            final boolean payAccruedOnDefault = xliPayAccruedOnDefault == 1 ? true : false;
            final TDateInterval ivl = ExcelFunctions.cdsStringToDateInterval(xlsCouponInterval);

            final TStubMethod tStubMethod = xlsStubType == null
                    ? ExcelFunctions.DEFAULT_STUB_METHOD
                    : ExcelFunctions.cdsStringToStubMethod(xlsStubType);

            final DayCount paymentDcc = xlsPaymentDcc == null
                    ? ExcelFunctions.DEFAULT_DAY_COUNT
                    : ExcelFunctions.cdsStringToDayCountConv(xlsPaymentDcc);

            final TBadDayConvention badDayConvention = xlsBadDayConvention == null
                    ? ExcelFunctions.DEFAULT_BAD_DAY_CONVENTION
                    : ExcelFunctions.cdsStringToBadDayConv(xlsBadDayConvention);

            final String holidays = xlsHolidays == null
                    ? ExcelFunctions.DEFAULT_HOLIDAY_CALENDAR : new String(xlsHolidays);

            final TCurve discountCurve = cdsCacheManager.get(xlhDiscountCurveKey);

            final double parSpread = xldParSpread;
            final double recoveryRate = xldRecoveryRate;
            final boolean isPriceClean = xliIsPriceClean == 1 ? true : false;

            DoubleHolder result = new DoubleHolder();
            if (CdsOne.cdsCdsoneUpfrontCharge(today,
                    valueDate,
                    benchmarkStartDate,
                    stepinDate,
                    startDate,
                    endDate,
                    couponRate,
                    payAccruedOnDefault,
                    ivl,
                    tStubMethod,
                    paymentDcc,
                    badDayConvention,
                    holidays,
                    discountCurve,
                    parSpread,
                    recoveryRate,
                    isPriceClean,
                    result) == ReturnStatus.FAILURE) {
                throw new CdsLibraryException("cdsUpfrontFlat::Error in CdsOne.cdsCdsoneUpfrontCharge");
            }

            return new XLNum(result.get());
        } catch (Exception ex) {
            logger.error(ex);
            return new XLString(ex.getMessage());
        }
    }

    @XLFunction(name = "APO.CDS.FeeLegFlows", args = {
            "StartDate",
            "EndDate",
            "Rate",
            "Notional",
            "CouponInterval",
            "StubType",
            "PaymentDCC",
            "BadDayConvention",
            "Holidays"
    }, argHelp = {
            "StartDate",
            "EndDate",
            "Rate",
            "Notional",
            "CouponInterval",
            "StubType",
            "PaymentDCC",
            "BadDayConvention",
            "Holidays"
    }, category = "APO CDS Functions")
    public static XLoper cdsFeeLegFlows(
            Double xldStartDate,
            Double xldEndDate,
            Double xldRate,
            Double xldNotional,
            String xlsCouponInterval,
            String xlsStubType,
            String xlsPaymentDcc,
            String xlsBadDayConvention,
            String xlsHolidays
    ) {
        try {
            Validate.notNull(xldStartDate, "Invalid Start date");
            Validate.notNull(xldEndDate, "Invalid End date");
            Validate.notNull(xldRate, "Invalid Rate");
            Validate.notNull(xldNotional, "Invalid Notional");
            Validate.notNull(xlsCouponInterval, "Invalid coupon interval");

            Validate.isTrue(!(Double.compare(xldStartDate, 0) == 0.0), "Invalid Start Date");
            Validate.isTrue(!(Double.compare(xldEndDate, 0) == 0.0), "Invalid End Date");
            Validate.isTrue(!(Double.compare(xldRate, 0) == 0.0), "Invalid Rate");
            Validate.isTrue(!(Double.compare(xldNotional, 0) == 0.0), "Invalid Notional");


            final LocalDate startDate = ExcelFunctions.xlDateToLocalDateTime(xldStartDate);
            final TBadDayConvention badDayConvention = xlsBadDayConvention == null
                    ? ExcelFunctions.DEFAULT_BAD_DAY_CONVENTION
                    : ExcelFunctions.cdsStringToBadDayConv(xlsBadDayConvention);

            final String calendar = xlsHolidays == null
                    ? ExcelFunctions.DEFAULT_HOLIDAY_CALENDAR
                    : xlsHolidays;

            final double couponRate = xldRate;
            final double notional = xldNotional;

            final TStubMethod stubType = xlsStubType == null
                    ? ExcelFunctions.DEFAULT_STUB_METHOD
                    : ExcelFunctions.cdsStringToStubMethod(xlsStubType);

            final DayCount paymentDcc = xlsPaymentDcc == null
                    ? ExcelFunctions.DEFAULT_DAY_COUNT
                    : ExcelFunctions.cdsStringToDayCountConv(xlsPaymentDcc);

            final LocalDate endDate = ExcelFunctions.xlDateToLocalDateTime(xldEndDate);

            boolean payAccruedOnDefault = true;
            boolean protectStart = true;

            TDateInterval couponInterval = ExcelFunctions.cdsStringToDateInterval(xlsCouponInterval);
            TFeeLeg feeLeg = new TFeeLeg(startDate,
                    endDate,
                    payAccruedOnDefault,
                    couponInterval,
                    stubType,
                    notional,
                    couponRate,
                    paymentDcc,
                    badDayConvention,
                    calendar,
                    protectStart
            );

            LocalDate[] accStartDates = feeLeg.getAccStartDates();
            LocalDate[] accEndDates = feeLeg.getAccEndDates();
            LocalDate[] paymentDates = feeLeg.getPayDates();

            DayCount feeLegDcc = feeLeg.getDcc();
            double feeLegCouponRate = feeLeg.getCouponRate();

            TFeeLegCashFlow[] feeLegCashFlows
                    = new TFeeLegCashFlow[feeLeg.getNbDates()];

            for (int i = 0; i < feeLeg.getNbDates(); i++) {
                double amount;
                double time;

                DoubleHolder result = new DoubleHolder();
                if (cdsDayCountFraction(accStartDates[i],
                        accEndDates[i],
                        feeLegDcc,
                        result) == ReturnStatus.FAILURE) {
                    throw new CdsLibraryException("CdsFunctionLibrary.cdsFeeLegFlows()::Error in TDateFunctions.cdsDayCountFraction");
                }

                time = result.get();
                amount = time * feeLegCouponRate * notional;

                TFeeLegCashFlow cashFlow = new TFeeLegCashFlow(paymentDates[i], amount);
                feeLegCashFlows[i] = cashFlow;
            }

            int rows = feeLegCashFlows.length;
            int cols = 2;

            XLArray array = new XLArray(rows, cols);
            for (int i = 0; i < rows; i++) {
                double xlPayDate = ExcelFunctions.localDateToExcelDate(feeLegCashFlows[i].getDate());
                double amt = feeLegCashFlows[i].getAmount();

                array.set(i, 0, xlPayDate);
                array.set(i, 1, amt);
            }
            return array;
        } catch (Exception ex) {
            logger.error(ex);
            return new XLString(ex.getMessage());
        }
    }

    @XLFunction(name = "APO.CDS.CleanSpreadCurveBuild", args = {
            "Today",
            "StartDate",
            "StepinDate",
            "CashSettleDate",
            "EndDates",
            "CouponRates",
            "IncludeFlags",
            "PayAccruedOnDefault",
            "CouponInterval",
            "StubType",
            "PaymentDCC",
            "BadDayConvention",
            "Holidays",
            "DiscountCurve",
            "RecoveryRate"
    }, argHelp = {
            "Today",
            "StartDate",
            "StepinDate",
            "CashSettleDate",
            "EndDates",
            "CouponRates",
            "IncludeFlags",
            "PayAccruedOnDefault",
            "CouponInterval",
            "StubType",
            "PaymentDCC",
            "BadDayConvention",
            "Holidays",
            "DiscountCurve",
            "RecoveryRate"
    }, category = "APO CDS Functions")
    public static XLoper cdsCleanSpreadCurveBuild(Double xldToday,
                                                  Double xldStartDate,
                                                  Double xldstepinDate,
                                                  Double xldCashSettleDate,
                                                  double[] xldaEndDates,
                                                  double[] xldaRates,
                                                  double[] xliaIncludeFlags,
                                                  Integer xliPayAccruedOnDefault,
                                                  String xlsCouponInterval,
                                                  String xlsStubType,
                                                  String xlsPaymentDcc,
                                                  String xlsBadDayConvention,
                                                  String xlsHolidays,
                                                  String xlhDiscountCurveKey,
                                                  Double xldRecoveryRate
    ) {
        try {
            Validate.notNull(xldToday, "Invalid Today date");
            Validate.notNull(xldStartDate, "Invalide Start date");
            Validate.notNull(xldstepinDate, "Invalid Stepin date");
            Validate.notNull(xldCashSettleDate, "Invalid Cash settle date");
            Validate.notNull(xldaEndDates, "Invalid End dates");
            Validate.notNull(xldaRates, "Invalid rates array");
            Validate.notNull(xliaIncludeFlags, "Invalid include flags");
            Validate.notNull(xliPayAccruedOnDefault, "Invalid accrued on default flag");


            final LocalDate today = ExcelFunctions.xlDateToLocalDateTime(xldToday);
            final LocalDate startDate = ExcelFunctions.xlDateToLocalDateTime(xldStartDate);
            final LocalDate stepinDate = ExcelFunctions.xlDateToLocalDateTime(xldstepinDate);
            final LocalDate cashSettleDate = ExcelFunctions.xlDateToLocalDateTime(xldCashSettleDate);
            final LocalDate[] endDates = ExcelFunctions.xlDatesToLocalDateTimeArray(xldaEndDates);
            final double[] rates = xldaRates;
            final Boolean[] includes = ExcelFunctions.xlDoublesToBooleanArray(xliaIncludeFlags);
            final boolean payAccruedOnDefault = xliPayAccruedOnDefault == 1 ? true : false;

            final TStubMethod stubType = xlsStubType == null
                    ? ExcelFunctions.DEFAULT_STUB_METHOD
                    : ExcelFunctions.cdsStringToStubMethod(xlsStubType);

            final DayCount paymentDcc =
                    xlsPaymentDcc == null
                            ? ExcelFunctions.DEFAULT_DAY_COUNT
                            : xlsPaymentDcc.equals("") ? ExcelFunctions.DEFAULT_DAY_COUNT : ExcelFunctions.cdsStringToDayCountConv(xlsPaymentDcc);

            final TBadDayConvention badDayConvention =
                    xlsBadDayConvention == null
                            ? ExcelFunctions.DEFAULT_BAD_DAY_CONVENTION
                            : ExcelFunctions.cdsStringToBadDayConv(xlsBadDayConvention);

            final TDateInterval couponInterval = ExcelFunctions.cdsStringToDateInterval(xlsCouponInterval);
            final String holidays = xlsHolidays == null ? ExcelFunctions.DEFAULT_HOLIDAY_CALENDAR : xlsHolidays;

            TCurve discountCurve = cdsCacheManager.get(xlhDiscountCurveKey);
            Validate.notNull(discountCurve);
            TCurve spreadCurve = CdsBootstrap.cdsCleanSpreadCurve(today,
                    discountCurve,
                    startDate,
                    stepinDate,
                    cashSettleDate,
                    endDates.length,
                    endDates,
                    rates,
                    includes,
                    xldRecoveryRate,
                    payAccruedOnDefault,
                    couponInterval,
                    paymentDcc,
                    stubType,
                    badDayConvention,
                    holidays
            );

            String tCurveKey = "TCurve@" + UUID.randomUUID().toString();
            cdsCacheManager.put(tCurveKey, spreadCurve);
            return new XLString(tCurveKey);
        } catch (Exception ex) {

            logger.error(ex);
            return new XLString(ex.getMessage());
        }

    }

    @XLFunction(name = "APO.CDS.DiscountFactor", args = {"CurveHandle", "Date"}, category = "APO CDS Functions")
    public static XLoper cdsDiscountFactor(
            String xlhCurveKey,
            Double xldDate

    ) {
        try {
            Validate.notNull(xldDate, "Invalid Curve Date");
            if (Double.compare(xldDate, 0) == 0.0)
                throw new CdsLibraryException("Invalid Curve Date");


            final TCurve curve = cdsCacheManager.get(xlhCurveKey);

            final LocalDate date = ExcelFunctions.xlDateToLocalDateTime(xldDate);
            final double result = TRateFunctions.cdsZeroPrice(curve, date);

            return new XLNum(result);

        } catch (Exception ex) {
            logger.error(ex);
            return new XLString(ex.getMessage());
        }
    }


    @XLFunction(name = "APO.CDS.ParSpreads", args = {
            "Today",
            "Stepin Date",
            "Start Date",
            "End Dates",
            "PayAccruedOnDefault",
            "CouponInterval",
            "StubType",
            "PaymentDcc",
            "BadDayConvention",
            "Holidays",
            "DiscountCurve",
            "SpreadCurve",
            "RecoveryRate"
    }, argHelp = {
            "Today",
            "Stepin Date",
            "Start Date",
            "End Dates",
            "PayAccruedOnDefault",
            "CouponInterval",
            "StubType",
            "PaymentDcc",
            "BadDayConvention",
            "Holidays",
            "DiscountCurve",
            "SpreadCurve",
            "RecoveryRate"
    }, category = "APO CDS Functions")
    public static XLoper cdsParSpreads(Double xldToday,
                                       Double xldStepinDate,
                                       Double xldStartDate,
                                       double[] xldEndDates,
                                       Integer xliPayAccruedOnDefault,
                                       String xlsCouponInterval,
                                       String xlsStubType,
                                       String xlsPaymentDcc,
                                       String xlsBadDayConvention,
                                       String xlsHolidays,
                                       String xlhDiscountCurveHandle,
                                       String xlhSpreadCurveHandle,
                                       Double xldRecoveryRate) {
        try {
            Validate.notNull(xldToday, "Invalid Today date");
            Validate.notNull(xldStepinDate, "Invalid Stepin date");
            Validate.notNull(xldStartDate, "Invalid Start date");
            Validate.notNull(xldEndDates, "Invalid End dates");
            Validate.notNull(xliPayAccruedOnDefault, "Invalid PayAccruedOnDefault");
            Validate.notNull(xlsCouponInterval, "Invalid coupon interval");
            Validate.notNull(xlhDiscountCurveHandle, "Invalid Discount curve handle");
            Validate.notNull(xlhSpreadCurveHandle, "Invalid Spread curve handle");
            Validate.notNull(xldRecoveryRate, "Invalid Recovery rate");

            Validate.isTrue(!(Double.compare(xldToday, 0) == 0.0), "Invalid Today date");
            Validate.isTrue(!(Double.compare(xldStepinDate, 0) == 0.0), "Invalid Stepin date");
            Validate.isTrue(!(Double.compare(xldStartDate, 0) == 0.0), "Invalid Start date");

            final LocalDate today = ExcelFunctions.xlDateToLocalDateTime(xldToday);
            final LocalDate stepinDate = ExcelFunctions.xlDateToLocalDateTime(xldStepinDate);
            final LocalDate startDate = ExcelFunctions.xlDateToLocalDateTime(xldStartDate);
            final LocalDate[] endDates = new LocalDate[xldEndDates.length];


            for (int i = 0; i < xldEndDates.length; i++)
                Validate.isTrue(!(Double.compare(xldEndDates[i], 0) == 0.0), "Invalid entry in end dates");

            for (int i = 0; i < xldEndDates.length; i++)
                endDates[i] = ExcelFunctions.xlDateToLocalDateTime(xldEndDates[i]);

            final TStubMethod stubType = xlsStubType == null
                    ? ExcelFunctions.DEFAULT_STUB_METHOD
                    : ExcelFunctions.cdsStringToStubMethod(xlsStubType);

            final DayCount paymentDcc =
                    xlsPaymentDcc == null
                            ? ExcelFunctions.DEFAULT_DAY_COUNT
                            : ExcelFunctions.cdsStringToDayCountConv(xlsPaymentDcc);

            final TBadDayConvention badDayConvention =
                    xlsBadDayConvention == null
                            ? ExcelFunctions.DEFAULT_BAD_DAY_CONVENTION
                            : ExcelFunctions.cdsStringToBadDayConv(xlsBadDayConvention);

            final String holidays = (xlsHolidays == null) ? "None" : xlsHolidays;
            final TDateInterval couponInterval = ExcelFunctions.cdsStringToDateInterval(xlsCouponInterval);
            final TCurve discountCurve = cdsCacheManager.get(xlhDiscountCurveHandle);
            final TCurve spreadCurve = cdsCacheManager.get(xlhSpreadCurveHandle);

            DoubleHolder[] results = new DoubleHolder[endDates.length];

            XLArray array = new XLArray(results.length, 1);
            for (int i = 0; i < results.length; i++) {
                array.set(i, 0, new XLNum(results[i].get()));
            }

            return array;
        } catch (Exception ex) {
            logger.error(ex);
            return new XLString(ex.getMessage());
        }

    }

    @XLFunction(name = "APO.CDS.CdsPrice", args = {
            "Today",
            "ValueDate",
            "StepinDate",
            "StartDate",
            "EndDate",
            "CouponRate",
            "PayAccruedOnDefault",
            "CouponInterval",
            "StubType",
            "PaymentDcc",
            "BadDayConvention",
            "Holidays",
            "DiscountCurve",
            "SpreadCurve",
            "RecoveryRate",
            "IsPriceClean"
    }, argHelp = {
            "Today",
            "ValueDate",
            "StepinDate",
            "StartDate",
            "EndDate",
            "CouponRate",
            "PayAccruedOnDefault",
            "CouponInterval",
            "StubType",
            "PaymentDcc",
            "BadDayConvention",
            "Holidays",
            "DiscountCurve",
            "SpreadCurve",
            "RecoveryRate",
            "IsPriceClean"
    }, category = "APO CDS Functions")
    public static XLoper cdsCdsPrice(Double xldToday,
                                     Double xldValueDate,
                                     Double xldstepinDate,
                                     Double xldStartDate,
                                     Double xldEndDate,
                                     Double xldCouponRate,
                                     Integer xliPayAccruedOnDefault,
                                     String xlsCouponInterval,
                                     String xlsStubType,
                                     String xlsPaymentDcc,
                                     String xlsBadDayConvention,
                                     String xlsHolidays,
                                     String xlhDiscountCurveKey,
                                     String xlhSpreadCurveKey,
                                     Double xldRecoveryRate,
                                     Integer xliIsPriceClean) {

        try {
            Validate.notNull(xldToday, "Invalid todayDate");
            Validate.notNull(xldValueDate, "Invalid valueDate");

            Validate.notNull(xldstepinDate, "Invalid stepinDate");
            Validate.notNull(xldStartDate, "Invalid startDate");
            Validate.notNull(xldEndDate, "Invalid endDate");
            Validate.notNull(xldCouponRate, "Invalid couponRate");
            Validate.notNull(xliPayAccruedOnDefault, "Invalid payAccruedOnDefault");
            Validate.notNull(xlsCouponInterval, "Invalid couponInterval");
            Validate.notNull(xlhDiscountCurveKey, "Invalid discount curve handle");
            Validate.notNull(xlhSpreadCurveKey, "Invalid spread curve handle");
            Validate.notNull(xldRecoveryRate, "Invalid recoveryRate");
            Validate.notNull(xliIsPriceClean, "Invalid isPriceClean");

            Validate.isTrue(!(Double.compare(xldToday, 0) == 0.0), "Invalid todayDate");
            Validate.isTrue(!(Double.compare(xldValueDate, 0) == 0.0), "Invalid valueDate");

            Validate.isTrue(!(Double.compare(xldstepinDate, 0) == 0.0), "Invalid stepinDate");
            Validate.isTrue(!(Double.compare(xldStartDate, 0) == 0.0), "Invalid startDate");
            Validate.isTrue(!(Double.compare(xldEndDate, 0) == 0.0), "Invalid endDate");


            final LocalDate today = ExcelFunctions.xlDateToLocalDateTime(xldToday);
            final LocalDate valueDate = ExcelFunctions.xlDateToLocalDateTime(xldValueDate);

            final LocalDate stepinDate = ExcelFunctions.xlDateToLocalDateTime(xldstepinDate);
            final LocalDate startDate = ExcelFunctions.xlDateToLocalDateTime(xldStartDate);
            final LocalDate endDate = ExcelFunctions.xlDateToLocalDateTime(xldEndDate);
            final double couponRate = xldCouponRate;
            final boolean payAccruedOnDefault = xliPayAccruedOnDefault == 1 ? true : false;
            final TDateInterval ivl = ExcelFunctions.cdsStringToDateInterval(xlsCouponInterval);

            final TStubMethod tStubMethod = xlsStubType == null
                    ? ExcelFunctions.DEFAULT_STUB_METHOD
                    : ExcelFunctions.cdsStringToStubMethod(xlsStubType);

            final DayCount paymentDcc = xlsPaymentDcc == null
                    ? ExcelFunctions.DEFAULT_DAY_COUNT
                    : ExcelFunctions.cdsStringToDayCountConv(xlsPaymentDcc);

            final TBadDayConvention badDayConvention = xlsBadDayConvention == null
                    ? ExcelFunctions.DEFAULT_BAD_DAY_CONVENTION
                    : ExcelFunctions.cdsStringToBadDayConv(xlsBadDayConvention);

            final String holidays = xlsHolidays == null
                    ? ExcelFunctions.DEFAULT_HOLIDAY_CALENDAR
                    : new String(xlsHolidays);

            final TCurve discountCurve = cdsCacheManager.get(xlhDiscountCurveKey);
            final TCurve spreadCurve = cdsCacheManager.get(xlhSpreadCurveKey);

            final double recoveryRate = xldRecoveryRate;
            final boolean isPriceClean = xliIsPriceClean == 1 ? true : false;
            final DoubleHolder result = new DoubleHolder();
            if (CdsOne.cdsCdsPrice(today,
                    valueDate,
                    stepinDate,
                    startDate,
                    endDate,
                    couponRate,
                    payAccruedOnDefault,
                    ivl,
                    tStubMethod,
                    paymentDcc,
                    badDayConvention,
                    holidays,
                    discountCurve,
                    spreadCurve,
                    recoveryRate,
                    isPriceClean,
                    result
            ).equals(ReturnStatus.FAILURE)) {
                throw new CdsLibraryException("CdsFunctionLibrary.cdsCdsPrice()::error in CdsOne.cdsCdsPrice");
            }

            return new XLNum(result.get());
        } catch (Exception ex) {
            logger.error(ex);
            return new XLString(ex.getMessage());
        }

    }

    @XLFunction(name = "APO.CDS.DatesAndRates", args = {"Curve"}, argHelp = {"CurveHandle"})
    public static XLoper cdsDatesAndRates(String xlhCurveHandle) {
        try {
            TCurve tCurve = cdsCacheManager.get(xlhCurveHandle);
            LocalDate[] dates = tCurve.getDates();
            double[] rates = tCurve.getRates();

            XLArray xlArray = new XLArray(dates.length, 2);

            for (int i = 0; i < dates.length; i++) {
                double excelDate = ExcelFunctions.localDateToExcelDate(dates[i]);
                double rate = rates[i];
                xlArray.set(i, 0, excelDate);
                xlArray.set(i, 1, rate);
            }

            return xlArray;
        } catch (Exception ex) {
            logger.error(ex);
            return new XLString(ex.getMessage());
        }

    }
}
