package com.anand.analytics.isdamodel.utils;

/**
 * Created by Anand on 10/22/2014.
 */
public enum DayCount {

    ACT_365(1), ACT_365F(2), ACT_360(3), B30_360(4), B30E_360(5), ACT_ACT(6), CDS_EFFECTIVE_RATE(8), CDS_DCC_LAST(9);

    private int value;

    private DayCount(int value) {
        this.value = value;
    }
}
