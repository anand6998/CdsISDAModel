package com.anand.analytics.isdamodel.date;

import com.anand.analytics.isdamodel.context.XlServerSpringUtils;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;

import java.util.Map;

/**
 * Created by anand on 12/26/14.
 */
public class HolidayCalendarFactory {
    private Map<String, HolidayCalendar> holidayCalendars;

    public HolidayCalendarFactory(Map<String, HolidayCalendar> holidayCalendars) {
        this.holidayCalendars = holidayCalendars;
    }

    public HolidayCalendar getCalendar(String calendarName) {
        if (holidayCalendars.containsKey(calendarName))
            return holidayCalendars.get(calendarName);
        else
            return (HolidayCalendar) XlServerSpringUtils.getBeanByName("noHolidaysCalendar");

    }
}
