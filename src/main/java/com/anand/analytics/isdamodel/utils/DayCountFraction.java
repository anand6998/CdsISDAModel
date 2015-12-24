package com.anand.analytics.isdamodel.utils;

import com.anand.analytics.isdamodel.date.Day;

/**
 * Created by Anand on 10/22/2014.
 */
@Deprecated
public class DayCountFraction {
    public double getDCF(Day date1,
                         Day date2,
                         DayCount dayCount) {
        switch (dayCount) {
            case ACT_365F:
                return date1.getDaysBetween(date2) / 365.;
            case ACT_360:
                return date1.getDaysBetween(date2) / 360.;
            default:
                return date1.getDaysBetween(date2) / 365.;

        }

    }
}
