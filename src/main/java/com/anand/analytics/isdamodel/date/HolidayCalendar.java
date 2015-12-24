package com.anand.analytics.isdamodel.date;

import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;

import java.util.List;

/**
 * Created by anand on 12/26/14.
 */
public interface HolidayCalendar {
    public List<Day> getHolidays();
    public List<DayOfWeek> getWeekendDays();
    public Day getNextBusinessDay(Day input, int sign);
    public Day addBusinessDays(Day input, long numBusDays);
    public Day getNextBusinessDay(Day input, TBadDayConvention badDayConvention) throws CdsLibraryException;
    public Day[] adjustBusinessDays(Day[] inputs, TBadDayConvention badDayConvention) throws CdsLibraryException;
    public boolean isHoliday(Day input);
}
