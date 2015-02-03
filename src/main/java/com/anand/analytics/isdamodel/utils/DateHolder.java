package com.anand.analytics.isdamodel.utils;

import com.anand.analytics.isdamodel.date.Day;

/**
 * Created by Anand on 10/27/2014.
 */
public class DateHolder {
    private Day date;

    public DateHolder() {
    }

    public void set(Day date) {
        this.date = date;
    }

    public Day get() {
        return date;
    }
}
