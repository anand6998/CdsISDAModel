package com.anand.analytics.isdamodel.date;

/**
 * Created by Anand on 1/16/2015.
 */

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Anand
 * Date: Jan 27, 2012
 * Time: 9:36:22 PM
 * To change this template use File | Settings | File Templates.
 */

public class Day implements Serializable, Comparable<Day>, Cloneable {

    private static final long serialVersionUID = 562963599105317855L;
    protected int year, day, month;

    public int jdn; //Julian Day Number

    int monthDays[] =
            { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    int leapMonthDays[] =
            {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public boolean isLeapYear() {
        boolean retValue = false;
        if (year % 4 == 0)
            retValue = true;

        if (year % 100 == 0) {
            if (year % 400 == 0)
                retValue = true;
            else
                retValue = false;
        }

        return retValue;
    }
    public Day() {
        Calendar calendar = new GregorianCalendar();

        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.day = calendar.get(Calendar.DAY_OF_MONTH);

        jdn = compute();

    }

    public Day(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        jdn = compute();
    }


    public Day(java.util.Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);

        jdn = compute();
    }

    private void invert(int julian, IntHolder year, IntHolder month, IntHolder day) {

        int i, j, k, l, n;

        l = julian + 68569;
        n = 4 * l / 146097;
        l = l - (146097 * n + 3) / 4;
        i = (4000 * (l + 1)) / 1461001;
        l = l - (1461 * i) / 4 + 31;
        j = (80 * l) / 2447;
        k = l - (int) ((2447 * j) / 80);
        l = j / 11;
        j = j + 2 - (12 * l);
        i = 100 * (n - 49) + i + l;

        year.set(i);
        month.set(j);
        day.set(k);
    }

    public Day(int julian) {

        IntHolder i = new IntHolder();
        IntHolder j = new IntHolder();
        IntHolder k = new IntHolder();

        invert(julian, i, j, k);
        year = i.get();
        month = j.get();
        day = k.get();

        jdn = julian;

    }

    protected int compute() {
        int a = (14 - month) / 12;
        int y = year + 4800 - a;
        int m = month + 12 * a - 3;

        int value = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045;
        this.jdn = value;
        return value;
    }

    public int getYear() {
        return year;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public Date getJavaDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }

    public int compareTo(Day o) {
        Day otherDay = (Day) o;

        if (jdn == otherDay.jdn)
            return 0;
        else
            return jdn > otherDay.jdn ? 1 : -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Day otherDay = (Day) o;

        if (day != otherDay.day) return false;
        if (month != otherDay.month) return false;
        if (year != otherDay.year) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = year;
        result = 31 * result + day;
        result = 31 * result + month;
        return result;
    }

    public String toString() {
        return new StringBuffer().append(year).append("-").append(format(month)).append("-").append(format(day))
                .toString();
    }

    protected String format(int day) {
        if (day < 10)
            return "0" + String.valueOf(day);
        else
            return String.valueOf(day);
    }

    public int getDaysBetween(Day otherDay) {

        return otherDay.jdn - jdn;
    }

    public Day addDays(int numberOfDays) {
        return new Day(jdn + numberOfDays);
    }

    public Day plusDays(int numberOfDays) { return new Day (jdn + numberOfDays); }

    public Day minusDays(int numberOfDays) {
        return new Day(jdn - numberOfDays);
    }

    public int getMonthValue() {
        return month;
    }

    public int getDayOfMonth() {
        return day;
    }

    public Day startDate() {
        return this;
    }

    public Day endDate() {
        return this;
    }


    public Day (int year, int month, int day, int jdn) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.jdn = jdn;
    }

    @Override
    public Day clone() {
        Day clonedDay = new Day(year, month, day, jdn);
        return clonedDay;
    }

    public DayOfWeek getDayOfWeek() {
        int dow = (jdn + 1) % 7;
        DayOfWeek dayOfWeek = DayOfWeek.of(dow);
        return dayOfWeek;
    }

    public boolean isWeekendDay() {
        //SUNDAY = 0 ; SATURDAY = 6
        int dayOfWeek = (jdn + 1) % 7;
        return (dayOfWeek == 0 || dayOfWeek == 6) ? true : false;
    }

    public boolean gt(Day otherDay) {
        return jdn > otherDay.jdn;
    }

    public boolean ge(Day otherDay) {
        return jdn >= otherDay.jdn;
    }

    public boolean lt(Day otherDay) {
        return jdn < otherDay.jdn;
    }

    public boolean le(Day otherDay) {
        return jdn <= otherDay.jdn;
    }

    public boolean eq(Day otherDay) {
        return jdn == otherDay.jdn;
    }

    public boolean isAfter(Day otherDay) { return jdn > otherDay.jdn; }

    public boolean isBefore(Day otherDay) { return jdn < otherDay.jdn; }

    public Day addMonths(int numMonths) {

        if (numMonths == 0) {
            return this;
        }

        long monthCount = year * 12L + (month - 1);
        long calcMonths = monthCount + numMonths;  // safe overflow
        int newYear = (int) calcMonths / 12;
        int newMonth = (int)calcMonths % 12 + 1;

        return new Day(newYear, newMonth, day);
    }

    public void inc() {
        ++jdn;

        IntHolder i = new IntHolder();
        IntHolder j = new IntHolder();
        IntHolder k = new IntHolder();

        invert(jdn, i, j, k);
        year = i.get();
        month = j.get();
        day = k.get();
    }


    public void dec() {
        --jdn;

        IntHolder i = new IntHolder();
        IntHolder j = new IntHolder();
        IntHolder k = new IntHolder();

        invert(jdn, i, j, k);
        year = i.get();
        month = j.get();
        day = k.get();

    }

    public boolean isEqual(Day otherDay) {
        return this.jdn == otherDay.jdn;
    }

    public int lengthOfMonth() {
        if (this.isLeapYear())
            return leapMonthDays[month - 1];
        return monthDays[month - 1];
    }
}
