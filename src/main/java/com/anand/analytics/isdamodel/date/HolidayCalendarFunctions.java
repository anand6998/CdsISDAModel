package com.anand.analytics.isdamodel.date;

import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;

import java.util.List;

import static com.anand.analytics.isdamodel.utils.CdsFunctions.ABS;
import static com.anand.analytics.isdamodel.utils.CdsFunctions.SIGN;

/**
 * Created by anand on 12/27/14.
 */
public class HolidayCalendarFunctions {

    public Day getNextBusinessDay(Day input, int sign, List<DayOfWeek> weekendDays, List<Day> holidays) {
        Day adjustedDate = input;
        DayOfWeek dayOfWeek = adjustedDate.getDayOfWeek();

        while (weekendDays.contains(dayOfWeek)|| holidays.contains(adjustedDate)) {
            adjustedDate = adjustedDate.plusDays(sign);
            dayOfWeek = adjustedDate.getDayOfWeek();
        }

        return adjustedDate;
    }

    public boolean isBusinessDay(Day input, List<DayOfWeek> weekendDays, List<Day> holidays) {
        DayOfWeek dayOfWeek = input.getDayOfWeek();
        if (weekendDays.contains(dayOfWeek) || holidays.contains(input))
            return false;
        return true;
    }

    public Day addBusinessDay(Day input, long sign, List<DayOfWeek> weekendDays, List<Day> holidays) {
        Day tmp = input.plusDays((int)sign);
        while(!isBusinessDay(tmp, weekendDays, holidays)) {
            tmp = tmp.plusDays((int)sign);
        }
        return tmp;
    }

    public Day addBusinessDays(Day input, long numBusDays, List<DayOfWeek> weekendDays, List<Day> holidays) {
        long intervalSign = SIGN(numBusDays);
        long numBusDaysLeft = ABS(numBusDays);

        if (weekendDays.size() == 0 && holidays.size() == 0) {
            //No adjustments at all
            Day result = input.plusDays((int)(intervalSign * numBusDaysLeft));
            return result;
        } else {
            Day tmp = input;
            for (int i = 0; i < numBusDays; i++) {
                tmp = addBusinessDay(tmp, intervalSign, weekendDays, holidays);
            }

            return tmp;
        }
    }

    public Day getNextBusinessDay(Day input, TBadDayConvention badDayConvention, List<DayOfWeek> weekendDays, List<Day> holidays)
            throws CdsLibraryException {
        Day retDate = input;
        int intervalSign = 1;
        switch(badDayConvention) {
            case NONE:
                break;
            case FOLLOW:
                intervalSign = 1;
                retDate = getNextBusinessDay(input, intervalSign, weekendDays, holidays);
                break;
            case PREVIOUS:
                intervalSign = -1;
                retDate = getNextBusinessDay(input, intervalSign, weekendDays, holidays);
                break;
            case MODIFIED:
                intervalSign = 1;
                retDate = getNextBusinessDay(input, intervalSign, weekendDays, holidays);

                if (input.getMonthValue() != retDate.getMonthValue()) {
                    retDate = getNextBusinessDay(input, -intervalSign, weekendDays, holidays);
                }

                break;
            default:
                throw new CdsLibraryException("Invalid badDayConvention");

        }

        return retDate;
    }


}
