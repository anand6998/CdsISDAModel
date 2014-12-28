package com.anand.analytics.isdamodel.date;

import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import com.anand.analytics.isdamodel.money.Currency;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.List;

/**
 * Created by anand on 12/26/14.
 */
public class DefaultHolidayCalendar implements HolidayCalendar {

    protected final List<LocalDate> holidays;
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


}
