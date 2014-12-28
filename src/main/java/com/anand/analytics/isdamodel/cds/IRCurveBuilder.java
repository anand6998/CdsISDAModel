package com.anand.analytics.isdamodel.cds;

import com.anand.analytics.isdamodel.date.HolidayCalendar;
import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import com.anand.analytics.isdamodel.domain.TCurve;
import com.anand.analytics.isdamodel.utils.DayCount;
import org.threeten.bp.LocalDate;

/**
 * Created by anand on 12/27/14.
 */
public class IRCurveBuilder {
    public static TCurve buildIRZeroCurve(
            LocalDate valueDate,
            char[] instrumentNames,
            LocalDate[] dates,
            double[] rates,
            long nInstr,
            DayCount mmDCC,
            long fixedSwapFreq,
            long floatSwapFreq,
            DayCount fixedSwapDayCount,
            DayCount floatSwapDayCount,
            TBadDayConvention badDayConvention,
            HolidayCalendar calendar
    ) {
        return null;
    }

}
