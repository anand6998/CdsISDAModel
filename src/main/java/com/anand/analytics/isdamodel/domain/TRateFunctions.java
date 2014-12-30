package com.anand.analytics.isdamodel.domain;

import com.anand.analytics.isdamodel.utils.CdsUtils;
import com.anand.analytics.isdamodel.utils.DayCount;
import com.anand.analytics.isdamodel.utils.DayCountBasis;
import com.anand.analytics.isdamodel.utils.DoubleHolder;
import com.anand.analytics.isdamodel.utils.IntHolder;
import com.anand.analytics.isdamodel.utils.ReturnStatus;
import org.apache.log4j.Logger;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import static com.anand.analytics.isdamodel.utils.CdsFunctions.IS_ALMOST_ZERO;


/**
 * Created by Anand on 12/4/2014.
 */
public class TRateFunctions {
    private static Logger logger = Logger.getLogger(TRateFunctions.class);
    public static double cdsForwardZeroPrice(TCurve zeroCurve, LocalDate startDate, LocalDate maturityDate) {
        double startPrice = cdsZeroPrice(zeroCurve, startDate);
        double maturityPrice = cdsZeroPrice(zeroCurve, maturityDate);
        return maturityPrice / startPrice;
    }

    public static ReturnStatus cdsRateToDiscountYearFrac(double rate, double yf, DayCountBasis rateBasis, DoubleHolder result) {
        switch(rateBasis) {
            case SIMPLE_BASIS:
            {
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
                }
                else {
                    double discount = Math.pow(tmp, -rateBasis.getValue() * yf);
                    result.set(discount);
                }

            }
        }

        return ReturnStatus.SUCCESS;
    }

    public static double cdsZeroPrice(TCurve zeroCurve, LocalDate date) {
        double zeroPrice = 0;
        double rate, time;

        rate = cdsZeroRate(zeroCurve, date);
        time = (zeroCurve.baseDate.periodUntil(date, ChronoUnit.DAYS)) / 365.;
        zeroPrice = Math.exp(-rate * time);
        return zeroPrice;
    }

    public static double cdsZeroRate(TCurve zeroCurve, LocalDate date) {
        ReturnStatus status = ReturnStatus.FAILURE;

        IntHolder exact = new IntHolder();
        IntHolder lo = new IntHolder();
        IntHolder hi = new IntHolder();

        DoubleHolder rate = new DoubleHolder();
        assert (zeroCurve != null);
        assert (zeroCurve.dates.length > 0);

        if (CdsUtils.binarySearchLong(date,
                zeroCurve.dates,
                exact,
                lo,
                hi).equals(ReturnStatus.FAILURE)) {
            throw new RuntimeException("Failed in cdsZeroRate");
        }

        if (exact.get() >= 0) {
            //date found in zero rates
            if (zcRateCC(zeroCurve, exact.get(), rate).equals(ReturnStatus.FAILURE)) {
                logger.error("Error in calculating cc rate");
                throw new RuntimeException("Error in calculating cc rate");
            }
        } else if (lo.get() < 0) {
            //date before start of zero dates
            if (zcRateCC(zeroCurve, 0, rate).equals(ReturnStatus.FAILURE)) {
                logger.error("Error in calculating cc rate");
                throw new RuntimeException("Error in calculating cc rate");
            }
        } else if (hi.get() >= zeroCurve.dates.length) {
            // date after end of zeroDates
            if (zeroCurve.dates.length == 1) {
                if (zcRateCC(zeroCurve, 0, rate).equals(ReturnStatus.FAILURE)) {
                    logger.error("Error in calculating cc rate");
                    throw new RuntimeException("Error in calculating cc rate");
                }
            } else {
                //extrapolate using last flat segment of the curve
                int loIdx = zeroCurve.dates.length - 2;
                int hiIdx = zeroCurve.dates.length - 1;

                if (zcInterpRate(zeroCurve, date, loIdx, hiIdx, rate).equals(ReturnStatus.FAILURE)) {
                    logger.error("Error in calculating cc rate");
                    throw new RuntimeException("Error in calculating cc rate");
                }

            }
        } else {
            //Date between start and end of zero dates
            if (zcInterpRate(zeroCurve, date, lo.get(), hi.get(), rate).equals(ReturnStatus.FAILURE)) {
                logger.error("Error in calculating cc rate");
                throw new RuntimeException("Error in calculating cc rate");
            }
        }
        return rate.get();
    }

    public static ReturnStatus zcInterpRate(TCurve zeroCurve, LocalDate date, int lo, int hi, DoubleHolder rate) {
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

        assert (t > t1);
        assert (t2 > t1);

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


}
