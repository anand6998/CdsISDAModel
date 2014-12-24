package com.anand.analytics.isdamodel.utils;

/**
 * Created by aanand on 10/22/2014.
 */
public enum DayCountBasis {
    CONTINUOUS_BASIS(5000), DISCOUNT_RATE(512), SIMPLE_BASIS(0), ANNUAL_BASIS(1), DISCOUNT_FACTOR(-2);
    private int value;

    private DayCountBasis(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
