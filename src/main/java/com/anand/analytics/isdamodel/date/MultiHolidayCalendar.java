package com.anand.analytics.isdamodel.date;

import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anand on 12/27/14.
 */
public class MultiHolidayCalendar implements HolidayCalendar {
    protected final List<LocalDate> holidays;
    protected final List<DayOfWeek> weekendDays;
    protected final HolidayListReader holidayListReader;
    protected final HolidayCalendarFunctions holidayCalendarFunctions;

    public MultiHolidayCalendar(List<HolidayCalendar> holidayCalendars, HolidayListReader reader, HolidayCalendarFunctions functions) {
        List<LocalDate> mergedHolidays = new ArrayList<>();
        List<DayOfWeek> mergedWeekends = new ArrayList<>();
        mergeHolidays(holidayCalendars, mergedHolidays, mergedWeekends);

        this.holidayCalendarFunctions = functions;
        this.holidayListReader = reader;
        this.holidays = mergedHolidays;
        this.weekendDays = mergedWeekends;

    }

    private void mergeHolidays(List<HolidayCalendar> holidayCalendars, List<LocalDate> mergedHolidays, List<DayOfWeek> mergedWeekends) {

        for (HolidayCalendar calendar : holidayCalendars) {
            List<LocalDate> holidaysForCalendar = calendar.getHolidays();
            for (LocalDate date : holidaysForCalendar) {
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
    public List<LocalDate> getHolidays() {
        return holidays;
    }

    @Override
    public List<DayOfWeek> getWeekendDays() {
        return weekendDays;
    }

    @Override
    public LocalDate getNextBusinessDay(LocalDate input, int sign) {
        return holidayCalendarFunctions.getNextBusinessDay(input, sign, weekendDays, holidays);
    }

    @Override
    public LocalDate addBusinessDays(LocalDate input, long numBusDays) {
        return holidayCalendarFunctions.addBusinessDays(input, numBusDays, weekendDays, holidays);
    }

    @Override
    public LocalDate getNextBusinessDay(LocalDate input, TBadDayConvention badDayConvention) throws CdsLibraryException {
        return holidayCalendarFunctions.getNextBusinessDay(input, badDayConvention, weekendDays, holidays);
    }

    @Override
    public LocalDate[] adjustBusinessDays(LocalDate[] inputs, TBadDayConvention badDayConvention) throws CdsLibraryException {
        LocalDate[] retList = new LocalDate[inputs.length];
        for (int idx = 0; idx < inputs.length; idx++)
            retList[idx] = getNextBusinessDay(inputs[idx], badDayConvention);

        return retList;
    }
}
