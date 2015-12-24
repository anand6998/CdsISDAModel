package com.anand.analytics.isdamodel.domain;

import com.anand.analytics.isdamodel.date.Day;

/**
 * Created by anand on 12/31/14.
 */
public class TBadDayList {
    int count;
    int maxAllowed;
    Day[] badDay;
    Day[] goodDay;

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

    public Day[] getBadDay() {
        return badDay;
    }

    public void setBadDay(Day[] badDay) {
        this.badDay = badDay;
    }

    public Day[] getGoodDay() {
        return goodDay;
    }

    public void setGoodDay(Day[] goodDay) {
        this.goodDay = goodDay;
    }
}
