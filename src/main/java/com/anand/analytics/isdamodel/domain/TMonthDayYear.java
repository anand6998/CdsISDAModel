package com.anand.analytics.isdamodel.domain;

/**
 * Created by anand on 12/30/14.
 */
class TMonthDayYear {
    int year, month, day;
    public boolean isLeap;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public boolean isLeap() {
        return isLeap;
    }

    public void setLeap(boolean isLeap) {
        this.isLeap = isLeap;
    }
}
