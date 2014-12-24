package com.anand.analytics.isdamodel.utils;

import org.threeten.bp.LocalDate;

/**
 * Created by aanand on 10/27/2014.
 */
public class DateHolder {
    private LocalDate date;

    public DateHolder() {
    }

    public void set(LocalDate date) {
        this.date = date;
    }

    public LocalDate get() {
        return date;
    }
}
