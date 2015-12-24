package com.anand.analytics.isdamodel.date;

import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.money.Currency;

import java.util.List;

/**
 * Created by anand on 12/26/14.
 */
public class DefaultHolidayCalendar implements HolidayCalendar {

    protected final List<Day> holidays;
    protected final List<DayOfWeek> weekendDays;
    protected final HolidayListReader holidayListReader;
    protected final HolidayCalendarFunctions holidayCalendarFunctions;
    protected final Currency currency;

    protected DefaultHolidayCalendar(List<String> holidayList, List<DayOfWeek> weekendDays, HolidayListReader reader,
                                     HolidayCalendarFunctions functions, Currency ccy) {
        this.holidayListReader = reader;
        this.holidays = holidayListReader.read(holidayList);
        this.weekendDays = weekendDays;
        this.holidayCalendarFunctions = functions;
        this.currency = ccy;
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
    public boolean isHoliday(Day input) {
        boolean isBusinessDay = holidayCalendarFunctions.isBusinessDay(input, weekendDays, holidays);
        if (!isBusinessDay)
            return true;
        return false;
    }

    @Override
    public Day[] adjustBusinessDays(Day[] inputs, TBadDayConvention badDayConvention) throws CdsLibraryException {
        Day[] retList = new Day[inputs.length];
        for (int idx = 0; idx < inputs.length; idx++)
            retList[idx] = getNextBusinessDay(inputs[idx], badDayConvention);

        return retList;
    }
}
