package com.anand.analytics.isdamodel.utils;

/**
 * Created by anand on 12/26/14.
 */
public class CdsFunctions {
    public static int SIGN(long value) {
        return value < 0 ? -1 : 1;
    }

    public static long ABS(long value) {
        return value < 0 ? value * -1 : value;
    }

    public static double MIN (double a, double b) {
        return a < b ? a : b;
    }
}
