package com.anand.analytics.isdamodel.utils;

import com.anand.analytics.isdamodel.domain.TDateInterval;
import com.anand.analytics.isdamodel.domain.TStubPos;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import org.apache.log4j.Logger;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

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
            LocalDate startDate,
            LocalDate maturityDate,
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

    public static ReturnStatus cdsCountDates(LocalDate fromDate, LocalDate toDate, TDateInterval interval, IntHolder numItervals, IntHolder extraDays) {
        try {
            boolean isValid = checkDateInterval(interval, fromDate, toDate);
            if (!isValid)
                return ReturnStatus.FAILURE;

            DoubleHolder intervalYears = new DoubleHolder();
            if (interval.getIntervalInYears(intervalYears).equals(ReturnStatus.FAILURE)) {
                logger.error("Error converting interval to years");
                return ReturnStatus.FAILURE;
            }

            double fromToYears = (double) (fromDate.periodUntil(toDate, ChronoUnit.DAYS)) / CdsDateConstants.DAYS_PER_YEAR;
            int lowNumIntervals = Math.max(0, (int) Math.floor(Math.abs(fromToYears / intervalYears.get())) - 2);
            int index = lowNumIntervals;
            LocalDate currDate = dateFromDateAndOffset(fromDate, interval, index);
            LocalDate lastDate = currDate;

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

            extraDays.set((int) Math.abs(lastDate.periodUntil(toDate, ChronoUnit.DAYS)));
        } catch (Exception ex) {
            logger.error(ex);
            return ReturnStatus.FAILURE;
        }

        return ReturnStatus.SUCCESS;
    }

    private static boolean checkDateInterval(TDateInterval interval, LocalDate fromDate, LocalDate toDate) {
        if (interval.prd == 0)
            return false;
        if (fromDate.periodUntil(toDate, ChronoUnit.DAYS) < 0) {
            logger.error("Invalid to and from dates");
            return false;
        }

        return true;
    }


}
