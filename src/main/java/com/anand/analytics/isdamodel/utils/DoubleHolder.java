package com.anand.analytics.isdamodel.utils;

/**
 * Created by aanand on 10/21/2014.
 */
public class DoubleHolder {
    private double value;

    public DoubleHolder() {
        this.value = 0;
    }

    public DoubleHolder(double value) {
        this.value = value;
    }

    public double get() {
        return value;
    }

    public void set(double value) {
        this.value = value;
    }
}
