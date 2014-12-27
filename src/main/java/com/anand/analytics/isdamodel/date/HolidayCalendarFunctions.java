package com.anand.analytics.isdamodel.date;

import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.List;

import static com.anand.analytics.isdamodel.utils.CdsFunctions.ABS;
import static com.anand.analytics.isdamodel.utils.CdsFunctions.SIGN;

/**
 * Created by anand on 12/27/14.
 */
public class HolidayCalendarFunctions {

    public LocalDate getNextBusinessDay(LocalDate input, int sign, List<DayOfWeek> weekendDays, List<LocalDate> holidays) {
        LocalDate adjustedDate = input;
        DayOfWeek dayOfWeek = adjustedDate.getDayOfWeek();

        while (weekendDays.contains(dayOfWeek)|| holidays.contains(adjustedDate)) {
            adjustedDate = adjustedDate.plusDays(sign);
            dayOfWeek = adjustedDate.getDayOfWeek();
        }

        return adjustedDate;
    }

    public boolean isBusinessDay(LocalDate input, List<DayOfWeek> weekendDays, List<LocalDate> holidays) {
        DayOfWeek dayOfWeek = input.getDayOfWeek();
        if (weekendDays.contains(dayOfWeek) || holidays.contains(input))
            return false;
        return true;
    }

    public LocalDate addBusinessDay(LocalDate input, long sign, List<DayOfWeek> weekendDays, List<LocalDate> holidays) {
        LocalDate tmp = input.plusDays(sign);
        while(!isBusinessDay(tmp, weekendDays, holidays)) {
            tmp = tmp.plusDays(sign);
        }
        return tmp;
    }

    public LocalDate addBusinessDays(LocalDate input, long numBusDays, List<DayOfWeek> weekendDays, List<LocalDate> holidays) {
        long intervalSign = SIGN(numBusDays);
        long numBusDaysLeft = ABS(numBusDays);

        if (weekendDays.size() == 0 && holidays.size() == 0) {
            //No adjustments at all
            LocalDate result = input.plusDays(intervalSign * numBusDaysLeft);
            return result;
        } else {
            LocalDate tmp = input;
            for (int i = 0; i < numBusDays; i++) {
                tmp = addBusinessDay(tmp, intervalSign, weekendDays, holidays);
            }

            return tmp;
        }
    }

    public LocalDate getNextBusinessDay(LocalDate input, TBadDayConvention badDayConvention, List<DayOfWeek> weekendDays, List<LocalDate> holidays)
            throws CdsLibraryException {
        LocalDate retDate = input;
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
