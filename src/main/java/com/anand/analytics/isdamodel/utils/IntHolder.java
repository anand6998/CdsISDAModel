package com.anand.analytics.isdamodel.utils;

/**
 * Created by Anand on 11/25/2014.
 */
public class IntHolder {
    private int value;

    public IntHolder() {
        this.value = 0;
    }

    public IntHolder(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        this.value = value;
    }
}
