package com.anand.analytics.isdamodel.utils;

import com.anand.analytics.isdamodel.domain.TDateInterval;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;

/**
 * Created by anand on 12/26/14.
 */
public class CdsFunctions {
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

    public static TDateInterval freq2TDateInterval(long freq) throws CdsLibraryException {
        if (freq > 0 && freq <=12) {
            long prd = CdsDateConstants.MONTHS_PER_YEAR / (int) freq;
            TDateInterval tDateInterval = new TDateInterval(prd, PeriodType.M, 0);
            return tDateInterval;
        }
        throw new CdsLibraryException("Bad frequency detected");
    }
}
