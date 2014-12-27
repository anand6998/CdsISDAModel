package com.anand.analytics.isdamodel.date;

import org.boris.xlloop.util.Day;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import static com.anand.analytics.isdamodel.utils.CdsFunctions.ABS;
import static com.anand.analytics.isdamodel.utils.CdsFunctions.SIGN;

/**
 * Created by anand on 12/26/14.
 */
public abstract class DefaultHolidayCalendar implements HolidayCalendar {

    protected List<LocalDate> holidays = new ArrayList<LocalDate>();
    protected List<DayOfWeek> weekendDays;

    protected DefaultHolidayCalendar(List<Day> holidayList, List<DayOfWeek> weekendDays) {

        for (int i = 0; i < holidayList.size(); i++) {
            Day day = holidayList.get(i);
            holidays.add(LocalDate.of(day.getYear(), day.getMonth(), day.getDay()));
        }
        this.weekendDays = weekendDays;
    }

    @Override
    public LocalDate getNextBusinessDay(LocalDate input, int sign) {
        LocalDate adjustedDate = input;
        DayOfWeek dayOfWeek = adjustedDate.getDayOfWeek();

        while (weekendDays.contains(dayOfWeek)|| holidays.contains(adjustedDate)) {
            adjustedDate = adjustedDate.plusDays(sign);
            dayOfWeek = adjustedDate.getDayOfWeek();
        }

        return adjustedDate;
    }

    public boolean isBusinessDay(LocalDate input) {
        DayOfWeek dayOfWeek = input.getDayOfWeek();
        if (weekendDays.contains(dayOfWeek) || holidays.contains(input))
            return false;
        return true;
    }

    public LocalDate addBusinessDay(LocalDate input, int sign) {
        LocalDate tmp = input.plusDays(sign);
        while(!isBusinessDay(tmp)) {
            tmp = tmp.plusDays(sign);
        }
        return tmp;
    }

    @Override
    public LocalDate addBusinessDays(LocalDate input, int numBusDays) {
        int intervalSign = SIGN(numBusDays);
        int numBusDaysLeft = ABS(numBusDays);

        if (weekendDays.size() == 0 && holidays.size() == 0) {
            //No adjustments at all
            LocalDate result = input.plusDays(intervalSign * numBusDaysLeft);
            return result;
        } else {
            LocalDate tmp = input;
            for (int i = 0; i < numBusDays; i++) {
                tmp = addBusinessDay(tmp, intervalSign);
            }

            return tmp;
        }
    }
}
