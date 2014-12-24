package com.anand.analytics.isdamodel.utils;

/**
 * Created by aanand on 10/21/2014.
 */
public class BooleanHolder {
    private boolean value;

    public BooleanHolder(boolean value) {
        this.value = value;
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }
}
