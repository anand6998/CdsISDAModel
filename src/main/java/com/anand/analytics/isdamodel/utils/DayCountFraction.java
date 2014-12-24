package com.anand.analytics.isdamodel.utils;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

/**
 * Created by Anand on 10/22/2014.
 */
@Deprecated
public class DayCountFraction {
    public double getDCF(LocalDate date1,
                         LocalDate date2,
                         DayCount dayCount) {
        switch (dayCount) {
            case ACT_365F:
                return date1.periodUntil(date2, ChronoUnit.DAYS) / 365.;
            case ACT_360:
                return date1.periodUntil(date2, ChronoUnit.DAYS) / 360.;
            default:
                return date1.periodUntil(date2, ChronoUnit.DAYS) / 365.;

        }

    }
}
