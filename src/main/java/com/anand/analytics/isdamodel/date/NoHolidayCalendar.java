package com.anand.analytics.isdamodel.date;

import org.boris.xlloop.util.Day;
import org.threeten.bp.DayOfWeek;

import java.util.List;

import static com.anand.analytics.isdamodel.utils.CdsDateConstants.DAYS_PER_WEEK;

/**
 * Created by anand on 12/26/14.
 */
public class NoHolidayCalendar extends DefaultHolidayCalendar {

    private final int numBusDaysPerWeek;

    public NoHolidayCalendar(List<Day> holidays, List<DayOfWeek> weekendDays) {
        super(holidays, weekendDays);
        numBusDaysPerWeek = DAYS_PER_WEEK - weekendDays.size();
    }
}
