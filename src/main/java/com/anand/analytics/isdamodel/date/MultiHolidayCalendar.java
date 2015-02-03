package com.anand.analytics.isdamodel.date;

import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anand on 12/27/14.
 */
public class MultiHolidayCalendar implements HolidayCalendar {
    protected final List<Day> holidays;
    protected final List<DayOfWeek> weekendDays;
    protected final HolidayListReader holidayListReader;
    protected final HolidayCalendarFunctions holidayCalendarFunctions;

    public MultiHolidayCalendar(List<HolidayCalendar> holidayCalendars, HolidayListReader reader, HolidayCalendarFunctions functions) {
        List<Day> mergedHolidays = new ArrayList<>();
        List<DayOfWeek> mergedWeekends = new ArrayList<>();
        mergeHolidays(holidayCalendars, mergedHolidays, mergedWeekends);

        this.holidayCalendarFunctions = functions;
        this.holidayListReader = reader;
        this.holidays = mergedHolidays;
        this.weekendDays = mergedWeekends;

    }

    private void mergeHolidays(List<HolidayCalendar> holidayCalendars, List<Day> mergedHolidays, List<DayOfWeek> mergedWeekends) {

        for (HolidayCalendar calendar : holidayCalendars) {
            List<Day> holidaysForCalendar = calendar.getHolidays();
            for (Day date : holidaysForCalendar) {
                if (!mergedHolidays.contains(date))
                    mergedHolidays.add(date);
            }

            List<DayOfWeek> weekendsForCalendar = calendar.getWeekendDays();
            for (DayOfWeek dayOfWeek : weekendsForCalendar) {
                if (!mergedWeekends.contains(dayOfWeek))
                    mergedWeekends.add(dayOfWeek);
            }
        }
    }

    @Override
    public List<Day> getHolidays() {
        return holidays;
    }

    @Override
    public List<DayOfWeek> getWeekendDays() {
        return weekendDays;
    }

    @Override
    public Day getNextBusinessDay(Day input, int sign) {
        return holidayCalendarFunctions.getNextBusinessDay(input, sign, weekendDays, holidays);
    }

    @Override
    public Day addBusinessDays(Day input, long numBusDays) {
        return holidayCalendarFunctions.addBusinessDays(input, numBusDays, weekendDays, holidays);
    }

    @Override
    public Day getNextBusinessDay(Day input, TBadDayConvention badDayConvention) throws CdsLibraryException {
        return holidayCalendarFunctions.getNextBusinessDay(input, badDayConvention, weekendDays, holidays);
    }

    @Override
    public Day[] adjustBusinessDays(Day[] inputs, TBadDayConvention badDayConvention) throws CdsLibraryException {
        Day[] retList = new Day[inputs.length];
        for (int idx = 0; idx < inputs.length; idx++)
            retList[idx] = getNextBusinessDay(inputs[idx], badDayConvention);

        return retList;
    }

    @Override
    public boolean isHoliday(Day input) {
        boolean isBusinessDay = holidayCalendarFunctions.isBusinessDay(input, weekendDays, holidays);
        if (!isBusinessDay)
            return true;
        return false;
    }
}
