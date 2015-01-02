package com.anand.analytics.isdamodel.date;

import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.List;

/**
 * Created by anand on 12/26/14.
 */
public interface HolidayCalendar {
    public List<LocalDate> getHolidays();
    public List<DayOfWeek> getWeekendDays();
    public LocalDate getNextBusinessDay(LocalDate input, int sign);
    public LocalDate addBusinessDays(LocalDate input, long numBusDays);
    public LocalDate getNextBusinessDay(LocalDate input, TBadDayConvention badDayConvention) throws CdsLibraryException;
    public LocalDate[] adjustBusinessDays(LocalDate[] inputs, TBadDayConvention badDayConvention) throws CdsLibraryException;
}
