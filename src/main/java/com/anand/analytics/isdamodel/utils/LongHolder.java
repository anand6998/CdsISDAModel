package com.anand.analytics.isdamodel.utils;

/**
 * Created by Anand on 12/1/2014.
 */
public class LongHolder {
    private long value;

    public LongHolder() {
        this.value = 0;
    }

    public LongHolder(long value) {
        this.value = value;
    }

    public long get() {
        return value;
    }

    public void set(long value) {
        this.value = value;
    }
}
