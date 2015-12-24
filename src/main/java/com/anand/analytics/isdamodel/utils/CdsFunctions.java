package com.anand.analytics.isdamodel.utils;

import com.anand.analytics.isdamodel.date.Day;
import com.anand.analytics.isdamodel.domain.TDateInterval;
import com.anand.analytics.isdamodel.domain.TStubPos;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import org.apache.log4j.Logger;

import static com.anand.analytics.isdamodel.domain.TDateFunctions.IS_BETWEEN;
import static com.anand.analytics.isdamodel.domain.TDateFunctions.dateFromDateAndOffset;

/**
 * Created by anand on 12/26/14.
 */
public class CdsFunctions {
    final static Logger logger = Logger.getLogger(CdsFunctions.class);

    private static double DBL_EPSILON = 2.2204460492503131e-16;

    public static int SIGN(long value) {
        return value < 0 ? -1 : 1;
    }

    public static long ABS(long value) {
        return value < 0 ? value * -1 : value;
    }

    public static double MIN (double a, double b) {
        return a < b ? a : b;
    }

    public static boolean IS_ALMOST_ZERO (double x) {
        if (x < DBL_EPSILON && x > DBL_EPSILON)
            return true;
        return false;
    }

    public static boolean ARE_ALMOST_EQUAL(double x, double y) {
        return IS_ALMOST_ZERO(x - y);
    }

    public static TDateInterval cdsFreq2TDateInterval(long freq) throws CdsLibraryException {
        if (freq > 0 && freq <=12) {
            long prd = CdsDateConstants.MONTHS_PER_YEAR / (int) freq;
            TDateInterval tDateInterval = new TDateInterval(prd, PeriodType.M, 0);
            return tDateInterval;
        }
        throw new CdsLibraryException("Bad frequency detected");
    }

    public static ReturnStatus cdsDateIntervalToFreq(final TDateInterval interval, DoubleHolder freq) {
        DoubleHolder years = new DoubleHolder();
        if (interval.getIntervalInYears(years).equals(ReturnStatus.FAILURE))
            return ReturnStatus.FAILURE;

        if (years.get() > 0.)
        {
            freq.set(1. / years.get());
            return ReturnStatus.SUCCESS;
        }

        logger.error("interval is zero");
        return ReturnStatus.FAILURE;
    }

    public static ReturnStatus cdsIsEndStub(
            Day startDate,
            Day maturityDate,
            TDateInterval ivl,
            TStubPos stubPos,
            BooleanHolder isEndStub
    ) {

        switch(stubPos) {
            case DEFAULT_BACK:
                isEndStub.set(true);
                break;
            case DEFAULT_FRONT:
                isEndStub.set(false);
                break;
            case DEFAULT_AUTO: {
                IntHolder extraDays =  new IntHolder();
                IntHolder numIntervals = new IntHolder();
                ReturnStatus status  = cdsCountDates(startDate,
                        maturityDate,
                        ivl,
                        numIntervals,
                        extraDays);
                if (status.equals( ReturnStatus.FAILURE)) {
                    logger.error("Error in cdsCountDates");
                    return ReturnStatus.FAILURE;
                }
                if (extraDays.get() > 0)
                    isEndStub.set(false);
                else
                    isEndStub.set(true);
            }
            break;
            default:
                isEndStub.set(false);
        }
        return ReturnStatus.SUCCESS;
    }

    public static ReturnStatus cdsCountDates(Day fromDate, Day toDate, TDateInterval interval, IntHolder numItervals, IntHolder extraDays) {
        try {
            boolean isValid = checkDateInterval(interval, fromDate, toDate);
            if (!isValid)
                return ReturnStatus.FAILURE;

            DoubleHolder intervalYears = new DoubleHolder();
            if (interval.getIntervalInYears(intervalYears).equals(ReturnStatus.FAILURE)) {
                logger.error("Error converting interval to years");
                return ReturnStatus.FAILURE;
            }

            double fromToYears = (double) (fromDate.getDaysBetween(toDate)) / CdsDateConstants.DAYS_PER_YEAR;
            int lowNumIntervals = Math.max(0, (int) Math.floor(Math.abs(fromToYears / intervalYears.get())) - 2);
            int index = lowNumIntervals;
            Day currDate = dateFromDateAndOffset(fromDate, interval, index);
            Day lastDate = currDate;

            while (IS_BETWEEN(currDate, fromDate, toDate)) {
                ++index;
                lastDate = currDate;
                currDate = dateFromDateAndOffset(fromDate, interval, index);
            }

            numItervals.set(index - 1);
            if (numItervals.get() < lowNumIntervals) {
                logger.error("Failed in countDates (" + fromDate + ")-( " + toDate + ")");
                return ReturnStatus.FAILURE;
            }

            extraDays.set((int) Math.abs(lastDate.getDaysBetween(toDate)));
        } catch (Exception ex) {
            logger.error(ex);
            return ReturnStatus.FAILURE;
        }

        return ReturnStatus.SUCCESS;
    }

    private static boolean checkDateInterval(TDateInterval interval, Day fromDate, Day toDate) {
        if (interval.prd == 0)
            return false;
        if (fromDate.getDaysBetween(toDate) < 0) {
            logger.error("Invalid to and from dates");
            return false;
        }

        return true;
    }

    public static <T extends Comparable<T>> ReturnStatus cdsBinarySearchLongFast(
            T xDesired,
            T[] xs,
            IntHolder loIdx,
            IntHolder hiIdx
    ) {
        int lo, hi, mid = 0;
        int count;

        int N = xs.length;
        if (N < 2) {
            if (N < 1) {
                logger.error("# points must be >= 1");
                return ReturnStatus.FAILURE;
            }
            else {
                loIdx.set(0);
                hiIdx.set(0);
                return ReturnStatus.SUCCESS;
            }
        }

        /*
         * Extrapolate if desired X is less than the smallest in X array
         */
        if (xDesired.compareTo(xs[0]) <= 0) {
            loIdx.set(0);
            hiIdx.set(1);
            return ReturnStatus.SUCCESS;
        }

        /*
         * Extrapolate if desired X is greater than the biggest in X array
         */
        if (xDesired.compareTo(xs[N - 1]) > 0) {
            loIdx.set(N - 2);
            hiIdx.set(N - 1);
            return ReturnStatus.SUCCESS;
        }

        lo = 0;
        hi = N - 2;

        /*
         * Do binary search to find pair of x's which surround the desired X value
         */
        for (count = N + 1; count > 0; count--) {
            mid = (hi + lo ) / 2;

            if (xDesired.compareTo(xs[mid]) < 0)
                hi = mid - 1;

            else if (xDesired.compareTo(xs[mid + 1]) > 0 )
                lo = mid + 1;

            else
                break;
        }

        if (count == 0) {
            logger.error(" x array not in increasing order");
            return ReturnStatus.FAILURE;
        }

        /*
         * Protect against a run of x values which are the same
         * set 2 surrounding indices to be lo and hi
         * Note that there is no danger of running off the end
         * since the only way for x[lo] = x[hi] is for both to be equal to xDesired.
         * But from check at beginning we know x[N-1] <> xDesired
         */

        lo = mid;
        hi = mid + 1;
        while (xs[lo].compareTo(xs[hi]) == 0)
            hi++;

        loIdx.set(lo);
        hiIdx.set(hi);

        return ReturnStatus.SUCCESS;
    }


}
