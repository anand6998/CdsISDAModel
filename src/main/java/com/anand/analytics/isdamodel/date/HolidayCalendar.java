package com.anand.analytics.isdamodel.date;

import org.threeten.bp.LocalDate;

/**
 * Created by anand on 12/26/14.
 */
public interface HolidayCalendar {
    public LocalDate getNextBusinessDay(LocalDate input, int sign);
    public LocalDate addBusinessDays(LocalDate input, int numBusDays);
}
