package com.anand.analytics.isdamodel.utils;

/**
 * Created by Anand on 10/22/2014.
 */
public enum DayCountBasis {
    CONTINUOUS_BASIS(5000, 1), DISCOUNT_RATE(512, 2), SIMPLE_BASIS(0, 3), ANNUAL_BASIS(1, 4), DISCOUNT_FACTOR(-2, 5);
    private int value;
    private int index;

    private DayCountBasis(int value, int idx) {
        this.value = value;
        this.index = idx;
    }

    public int getValue() {
        return value;
    }

    public static DayCountBasis get(int idx) {
        for (int i = 0; i < values().length; i++)
            if (values()[i].index == idx)
                return values()[i];
        return null;
    }

    public int index() {
        return index;
    }
}
