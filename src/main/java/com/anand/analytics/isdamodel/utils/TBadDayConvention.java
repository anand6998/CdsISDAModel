package com.anand.analytics.isdamodel.utils;

/**
 * Created by Anand on 10/24/2014.
 */
public enum TBadDayConvention {
    FOLLOW, PREVIOUS, NONE, MODIFIED;

    public static TBadDayConvention get(int n) {
        switch (n) {
            case 80:
                return PREVIOUS;
            case 70:
                return FOLLOW;
            case 78:
                return NONE;
            case 77:
                return MODIFIED;
            default:
                return MODIFIED;

        }
    }


}
