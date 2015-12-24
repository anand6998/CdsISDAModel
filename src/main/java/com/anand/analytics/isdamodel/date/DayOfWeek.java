package com.anand.analytics.isdamodel.date;

import com.anand.analytics.isdamodel.exception.CdsLibraryException;

/**
 * Created by Anand on 1/17/2015.
 */
public enum DayOfWeek {
    SUN, MON, TUE, WED, THU, FRI, SAT;

    private static final DayOfWeek[] ENUMS = DayOfWeek.values();

    public static DayOfWeek of(int dayOfWeek) {
        if (dayOfWeek < 0 || dayOfWeek > 6) {
            throw new CdsLibraryException("Invalid value for DayOfWeek: " + dayOfWeek);
        }
        return ENUMS[dayOfWeek];
    }

    public int getValue() {
        return ordinal() + 1;
    }

    public DayOfWeek plus(long days) {
        int amount = (int) (days % 7);
        return ENUMS[(ordinal() + (amount + 7)) % 7];
    }

    public DayOfWeek minus(long days) {
        return plus(-(days % 7));
    }
}
