package com.anand.analytics.isdamodel.domain;

import org.threeten.bp.LocalDate;

/**
 * Created by anand on 12/31/14.
 */
public class TBadDayList {
    int count;
    int maxAllowed;
    LocalDate[] badDay;
    LocalDate[] goodDay;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }

    public void setMaxAllowed(int maxAllowed) {
        this.maxAllowed = maxAllowed;
    }

    public LocalDate[] getBadDay() {
        return badDay;
    }

    public void setBadDay(LocalDate[] badDay) {
        this.badDay = badDay;
    }

    public LocalDate[] getGoodDay() {
        return goodDay;
    }

    public void setGoodDay(LocalDate[] goodDay) {
        this.goodDay = goodDay;
    }
}
