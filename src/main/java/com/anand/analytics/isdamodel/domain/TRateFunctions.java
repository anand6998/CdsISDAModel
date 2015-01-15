package com.anand.analytics.isdamodel.domain;

import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.ir.ZeroCurve;
import com.anand.analytics.isdamodel.utils.*;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.threeten.bp.LocalDate;
import org.threeten.bp.chrono.ChronoLocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import static com.anand.analytics.isdamodel.utils.CdsFunctions.ARE_ALMOST_EQUAL;
import static com.anand.analytics.isdamodel.utils.CdsFunctions.IS_ALMOST_ZERO;


/**
 * Created by Anand on 12/4/2014.
 */
public class TRateFunctions {
    private static Logger logger = Logger.getLogger(TRateFunctions.class);

    public static double cdsForwardZeroPrice(TCurve zeroCurve, LocalDate startDate, LocalDate maturityDate) throws CdsLibraryException {
        double startPrice = cdsZeroPrice(zeroCurve, startDate);
        double maturityPrice = cdsZeroPrice(zeroCurve, maturityDate);
        return maturityPrice / startPrice;
    }

    public static ReturnStatus cdsRateToDiscountYearFrac(double rate, double yf, DayCountBasis rateBasis, DoubleHolder result) {
        switch (rateBasis) {
            case SIMPLE_BASIS: {
                double denom = 1.0 + rate * yf;
                if (denom <= 0.0 || IS_ALMOST_ZERO(denom)) {
                    logger.error("Invalid simple interest rate");
                    return ReturnStatus.FAILURE;
                }

                result.set(1.0 / denom);
            }
            break;
            case DISCOUNT_RATE: {
                if (IS_ALMOST_ZERO(yf)) {
                    result.set(1.0);

                } else {
                    double discount = 1.0 - rate * yf;
                    if (discount <= 0.0) {
                        logger.error("Invalid discount rate");
                        return ReturnStatus.FAILURE;
                    }

                    result.set(discount);
                }
            }
            break;
            case CONTINUOUS_BASIS: {
                double discount = Math.exp(-rate * yf);
                result.set(discount);
            }
            break;
            case DISCOUNT_FACTOR:
                result.set(rate);
                break;
            default: {
                double tmp = 1.0 + rate / rateBasis.getValue();
                if (tmp <= 0.0 || IS_ALMOST_ZERO(tmp)) {
                    logger.error("Bad rate");
                    return ReturnStatus.FAILURE;
                } else {
                    double discount = Math.pow(tmp, -rateBasis.getValue() * yf);
                    result.set(discount);
                }

            }
        }

        return ReturnStatus.SUCCESS;
    }

    public static double cdsZeroPrice(TCurve zeroCurve, LocalDate date) throws CdsLibraryException {
        double zeroPrice = 0;
        double rate, time;

        rate = cdsZeroRate(zeroCurve, date);
        time = (zeroCurve.getBaseDate().periodUntil(date, ChronoUnit.DAYS)) / 365.;
        zeroPrice = Math.exp(-rate * time);
        return zeroPrice;
    }

    public static double cdsZeroRate(TCurve zeroCurve, LocalDate date) throws CdsLibraryException {
        try {
            ReturnStatus status = ReturnStatus.FAILURE;

            IntHolder exact = new IntHolder();
            IntHolder lo = new IntHolder();
            IntHolder hi = new IntHolder();

            DoubleHolder rate = new DoubleHolder();
            Validate.notNull(zeroCurve, "zeroCurve is null");
            Validate.isTrue(zeroCurve.getDates().length > 0, "zeroCurve.dates.length == 0");

            if (CdsUtils.binarySearchLong(date,
                    zeroCurve.getDates(),
                    exact,
                    lo,
                    hi).equals(ReturnStatus.FAILURE)) {
                throw new CdsLibraryException("Failed in cdsZeroRate");
            }

            if (exact.get() >= 0) {
                //date found in zero rates
                if (zcRateCC(zeroCurve, exact.get(), rate).equals(ReturnStatus.FAILURE)) {
                    logger.error("Error in calculating cc rate");
                    throw new CdsLibraryException("Error in calculating cc rate");
                }
            } else if (lo.get() < 0) {
                //date before start of zero dates
                if (zcRateCC(zeroCurve, 0, rate).equals(ReturnStatus.FAILURE)) {
                    logger.error("Error in calculating cc rate");
                    throw new CdsLibraryException("Error in calculating cc rate");
                }
            } else if (hi.get() >= zeroCurve.dates.length) {
                // date after end of zeroDates
                if (zeroCurve.dates.length == 1) {
                    if (zcRateCC(zeroCurve, 0, rate).equals(ReturnStatus.FAILURE)) {
                        logger.error("Error in calculating cc rate");
                        throw new CdsLibraryException("Error in calculating cc rate");
                    }
                } else {
                    //extrapolate using last flat segment of the curve
                    int loIdx = zeroCurve.dates.length - 2;
                    int hiIdx = zeroCurve.dates.length - 1;

                    if (zcInterpRate(zeroCurve, date, loIdx, hiIdx, rate).equals(ReturnStatus.FAILURE)) {
                        logger.error("Error in calculating cc rate");
                        throw new CdsLibraryException("Error in calculating cc rate");
                    }

                }
            } else {
                //Date between start and end of zero dates
                if (zcInterpRate(zeroCurve, date, lo.get(), hi.get(), rate).equals(ReturnStatus.FAILURE)) {
                    logger.error("Error in calculating cc rate");
                    throw new CdsLibraryException("Error in calculating cc rate");
                }
            }
            return rate.get();
        } catch (Exception ex) {
            logger.error(ex);
            throw new CdsLibraryException(ex.getMessage());
        }
    }

    public static ReturnStatus zcInterpRate(TCurve zeroCurve, LocalDate date, int lo, int hi, DoubleHolder rate) {
        try {
            long t1;
            long t2;
            long t;
            double z1t1;
            double z2t2;
            DoubleHolder z1 = new DoubleHolder();
            DoubleHolder z2 = new DoubleHolder();


            t1 = zeroCurve.baseDate.periodUntil(zeroCurve.dates[lo], ChronoUnit.DAYS);
            t2 = zeroCurve.baseDate.periodUntil(zeroCurve.dates[hi], ChronoUnit.DAYS);

            t = zeroCurve.baseDate.periodUntil(date, ChronoUnit.DAYS);

            Validate.isTrue(t > t1, "t <= t1");
            Validate.isTrue(t2 > t1, " t2 <= t1");

            if (zcRateCC(zeroCurve, lo, z1).equals(ReturnStatus.FAILURE)) {
                logger.error("Error in calculating interp rate");
                return ReturnStatus.FAILURE;
            }
            if (zcRateCC(zeroCurve, hi, z2).equals(ReturnStatus.FAILURE)) {
                logger.error("Error in calculating interp rate");
                return ReturnStatus.FAILURE;
            }

            z1t1 = z1.get() * t1;
            z2t2 = z2.get() * t2;

            if (t == 0) {
                /**
                 * If date equals the base date, then the zero rate is undefined and irrelevant.
                 * So let us get the rate for the following day which is in the right ballpart at any rate
                 * An exception to this rule is when t2 = 0 as wll. In this case rate = z2
                 */
                if (t2 == 0) {
                    rate = z2;
                    return ReturnStatus.SUCCESS;
                }

                t = 1;
            }

            double zt = z1t1 + (z2t2 - z1t1) * (t - t1) / (t2 - t1);
            rate.set(zt / t);
            return ReturnStatus.SUCCESS;
        } catch (Exception ex) {
            logger.error(ex);
            return ReturnStatus.FAILURE;
        }

    }

    public static ReturnStatus zcRateCC(TCurve zeroCurve, int idx, DoubleHolder ccRate) {
        return cdsConvertCompoundRate(zeroCurve.rates[idx],
                zeroCurve.basis,
                zeroCurve.dayCountConv,
                DayCountBasis.CONTINUOUS_BASIS,
                DayCount.ACT_365F,
                ccRate);
    }

    public static ReturnStatus cdsConvertCompoundRate(double inRate,
                                                      DayCountBasis inbasis,
                                                      DayCount inDayCountConv,
                                                      DayCountBasis outbasis,
                                                      DayCount outDayCountConv,
                                                      DoubleHolder outRate) {
        double ccRate;

        if (inbasis.equals(outbasis)) {
            if (inDayCountConv.equals(outDayCountConv)) {
                outRate.set(inRate);
            } else if (inDayCountConv.equals(DayCount.ACT_365F) && outDayCountConv.equals(DayCount.ACT_360)) {
                outRate.set(inRate * 360. / 365.);
            } else if (inDayCountConv.equals(DayCount.ACT_360) && outDayCountConv.equals(DayCount.ACT_365F)) {
                outRate.set(inRate * 365.0 / 360.0);
            } else {
                logger.error("Error in conversion");
                return ReturnStatus.FAILURE;
            }
        } else {
            double dayFactor = 1.;
            if (inDayCountConv.equals(outDayCountConv)) {
                dayFactor = 1.;
            } else if (inDayCountConv.equals(DayCount.ACT_365F) && outDayCountConv.equals(DayCount.ACT_360)) {
                dayFactor = 360. / 365.;
            } else if (inDayCountConv.equals(DayCount.ACT_360) && outDayCountConv.equals(DayCount.ACT_365F)) {
                dayFactor = 365.0 / 360.0;
            } else {
                logger.error("Unknown day count convention");
                return ReturnStatus.FAILURE;
            }

            /* convert inRate to ccRate then convert to outRate */
            if (inbasis.equals(DayCountBasis.CONTINUOUS_BASIS)) {
                ccRate = inRate * dayFactor;
            } else if (inbasis.getValue() >= 1.0 && inbasis.getValue() <= 365.) {
                ccRate = dayFactor * inbasis.getValue() * Math.log(1.0 + inRate / inbasis.getValue());
            } else {
                logger.error("Invalid Input basis");
                return ReturnStatus.FAILURE;
            }

            if (outbasis.equals(DayCountBasis.CONTINUOUS_BASIS)) {
                outRate.set(ccRate);
            } else if (outbasis.getValue() >= 1. && outbasis.getValue() <= 365.) {
                outRate.set(outbasis.getValue() * (Math.exp(ccRate / outbasis.getValue()) - 1.));
            } else {
                logger.error("Invalid Output basis");
                return ReturnStatus.FAILURE;
            }
        }

        return ReturnStatus.SUCCESS;
    }

    public static ReturnStatus zcComputeDiscount(ZeroCurve zc,
                                                 LocalDate date,
                                                 double rate, DoubleHolder discountOut) {
        if (zc.getDayCountBasis().equals(DayCountBasis.ANNUAL_BASIS) &&
                rate >= -1.0 &&
                (date.isEqual(zc.getValueDate()) || date.isAfter(zc.getValueDate())) &&
                (zc.getDayCount().equals(DayCount.ACT_365F) || zc.getDayCount().equals(DayCount.ACT_360))) {
            double discount = Math.pow(1 + rate, (date.periodUntil(zc.getValueDate(), ChronoUnit.DAYS) / (zc.getDayCount().equals(DayCount.ACT_360) ? 360. : 365.)));
            discountOut.set(discount);
            return ReturnStatus.SUCCESS;
        }

        DoubleHolder discOut = new DoubleHolder();
        if (cdsRateToDiscount(
                rate,
                zc.getValueDate(),
                date,
                zc.getDayCount(),
                zc.getDayCountBasis(), discOut).equals(ReturnStatus.FAILURE)
                ) {
            logger.error("Error calculating cdsRateToDiscount");
            return ReturnStatus.FAILURE;

        }

        discountOut.set(discOut.get());
        return ReturnStatus.SUCCESS;

    }

    private static ReturnStatus cdsRateToDiscount(double rate, LocalDate startDate, LocalDate endDate,
                                                  DayCount rateDayCountConv, DayCountBasis rateBasis, DoubleHolder discOut) {


        if (rateBasis.equals(DayCountBasis.DISCOUNT_FACTOR)) {
            if (rate <= 0.0) {
                logger.error("Bad rate");
                return ReturnStatus.FAILURE;
            }
            discOut.set(rate);
            return ReturnStatus.SUCCESS;
        }

        if (rateBasis.getValue() < DayCountBasis.SIMPLE_BASIS.getValue()) {
            logger.error("Bad basis");
            return ReturnStatus.FAILURE;
        }

        DoubleHolder rateYF = new DoubleHolder();

        if (TDateFunctions.cdsDayCountFraction(startDate, endDate, rateDayCountConv, rateYF).equals(ReturnStatus.FAILURE)) {
            logger.error("Error calculating cdsDayCountFraction");
            return ReturnStatus.FAILURE;
        }

        DoubleHolder discount = new DoubleHolder();
        if (cdsRateToDiscountYearFrac(rate, rateYF.get(), rateBasis, discount).equals(ReturnStatus.FAILURE)) {
            logger.error("Error calculating discount year fraction");
            return ReturnStatus.FAILURE;
        }

        discOut.set(discount.get());
        return ReturnStatus.SUCCESS;
    }

    public static ReturnStatus cdsDiscountToRate(double discount, LocalDate startDate, LocalDate endDate,
                                                 DayCount rateDayCountConv, DayCountBasis rateBasis, DoubleHolder rate)
            {
        if (discount <= 0.0) {
            logger.error("Bad discount factor");
            return ReturnStatus.FAILURE;
        }

        if (rateBasis.equals(DayCountBasis.DISCOUNT_FACTOR)) {
            if (startDate.isEqual(endDate)) {
                if (!ARE_ALMOST_EQUAL(discount, 1.0)) {
                    logger.error("startDate == endDate; discountFactor != 1");
                    return ReturnStatus.FAILURE;
                }

                rate.set(1.0);
                return ReturnStatus.SUCCESS;
            } else {
                rate.set(discount);
                return ReturnStatus.SUCCESS;
            }
        }

        if (startDate.isEqual(endDate)) {
            logger.error("startDate == endDate");
            return ReturnStatus.FAILURE;
        }

        if (rateBasis.getValue() < DayCountBasis.SIMPLE_BASIS.getValue()) {
            logger.error("Bad input basis");
            return ReturnStatus.FAILURE;
        }

        DoubleHolder rateYf = new DoubleHolder();
        if (TDateFunctions.cdsDayCountFraction(startDate, endDate, rateDayCountConv, rateYf).equals(ReturnStatus.FAILURE)) {
            logger.error("Error in cdsDayCountFraction");
            return ReturnStatus.FAILURE;
        }

        DoubleHolder rateOut = new DoubleHolder();
        if (cdsRateToDiscountYearFrac(discount, rateYf.get(), rateBasis, rateOut).equals(ReturnStatus.FAILURE)) {
            logger.error("Error in cdsRateToDiscountYearFraction");
            return ReturnStatus.FAILURE;
        }

        rate.set(rateOut.get());
        return ReturnStatus.SUCCESS;

    }

    /**
     * Calculates an interpolated rate for a date
     *
     * Note: piece wise interpolation allows different areas of the zero curve
     * to be interpolated using different methods
     *
     * Basically an array of <date, interpolationStuff> is used for any date before the given date
     * The code allows the interpolation stuff to be another piecewise interpolation type although the utility
     * of this is unknown
     * @param zc
     * @param date
     * @param interpTypeIn
     * @param interpDataIn
     * @return
     * @throws CdsLibraryException
     */
    public static ReturnStatus zcInterpolate (
            ZeroCurve zc,
            LocalDate date,
            TInterpType interpTypeIn,
            TInterpData interpDataIn,
            DoubleHolder rateOut
    ) {

        if (zc.getfNumItems() < 1) {
            logger.error("No points on the zero Curve");
            return ReturnStatus.FAILURE;
        }
        /*
        Do flat exptrapolation only when going backwards. This is done so that the swaps
        which have payments before the beginning of the stub zero curve will still value
        to par. This can happen very easily if there are swaps with front stubs.
        We still permit forward non-flat extrapolation
         */
        if (date.isBefore(zc.getDates()[0]) || date.isEqual(zc.getDates()[0])) {
            rateOut.set(zc.getRates()[0]);
            return ReturnStatus.SUCCESS;
        }

        if (date.isEqual(zc.getValueDate())) {
            /*
            cannot calculate rate for value date, so get the value at the next date
            which is the next date which is the next best thing
             */
            //TODO - check this
            date = date.plusDays(1);
        }

        /*
        Find indices in zero curve which bracket the date
         */
        IntHolder loIdx = new IntHolder();
        IntHolder hiIdx = new IntHolder();
        ChronoLocalDate xDesired = date;

        if (CdsFunctions.cdsBinarySearchLongFast(xDesired,
                zc.getDates(),
                loIdx,
                hiIdx).equals(ReturnStatus.FAILURE)) {
            logger.error("Binary search failed");
            return ReturnStatus.FAILURE;
        }

        int lo = loIdx.get();
        int hi = hiIdx.get();

        /* exact match */
        if (zc.getDates()[lo].compareTo(date) == 0) {
            rateOut.set(zc.getRates()[lo]);
            return ReturnStatus.SUCCESS;
        }
        if (zc.getDates()[hi].compareTo(date) == 0) {
            rateOut.set(zc.getRates()[hi]);
            return ReturnStatus.SUCCESS;
        }


        TInterpType interpType = interpTypeIn;
        TInterpData interpData = interpDataIn;

        double rate;

        //This should use the daycount convention to get days between
        switch(interpType) {
            case LINEAR_INTERP: {
                long hi_lo = zc.getDates()[lo].periodUntil(zc.getDates()[hi], ChronoUnit.DAYS);
                long dt_lo = zc.getDates()[lo].periodUntil(date, ChronoUnit.DAYS);

                rate = zc.getRates()[lo];
                if (hi_lo != 0) {
                    rate += ((zc.getRates()[hi] - zc.getRates()[lo]) / hi_lo) * dt_lo;
                }
            }
            break;

            case FLAT_FORWARDS: {

                long hi_lo = zc.getDates()[lo].periodUntil(zc.getDates()[hi], ChronoUnit.DAYS);
                long dt_lo = zc.getDates()[lo].periodUntil(date, ChronoUnit.DAYS);

                if (hi_lo == 0) {
                    rate = zc.getRates()[lo];
                } else {
                    DoubleHolder discLo = new DoubleHolder();
                    if(TRateFunctions.zcComputeDiscount(zc, zc.getDates()[lo], zc.getRates()[lo], discLo).equals(ReturnStatus.FAILURE)) {
                        logger.error("Error computing discount");
                        return ReturnStatus.FAILURE;
                    }

                    DoubleHolder discHi = new DoubleHolder();
                    if(TRateFunctions.zcComputeDiscount(zc, zc.getDates()[hi], zc.getRates()[hi], discHi).equals(ReturnStatus.FAILURE)) {
                        logger.error("Error computing discount");
                        return ReturnStatus.FAILURE;
                    }

                    if (discLo.get() == 0.0) {
                        logger.error("Zero discLo");
                        return ReturnStatus.FAILURE;
                    }

                    double discDate = discLo.get() * Math.pow((discHi.get() / discLo.get()), (dt_lo / (double) hi_lo));

                    DoubleHolder rateOut2 = new DoubleHolder();
                    if (TRateFunctions.cdsDiscountToRate(discDate, zc.getValueDate(), date, zc.getDayCount(), zc.getDayCountBasis(), rateOut2).equals(ReturnStatus.FAILURE)) {
                        logger.error("Error in cdsDiscountToRate");
                    }

                    rate = rateOut2.get();
                }
            }

            break;
            default:
                logger.error("Bad interpolation type");
                return ReturnStatus.FAILURE;
        }

        rateOut.set(rate);
        return  ReturnStatus.SUCCESS;
    }

}
