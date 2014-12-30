package com.anand.analytics.isdamodel.cds;

import com.anand.analytics.isdamodel.date.HolidayCalendar;
import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import com.anand.analytics.isdamodel.domain.TCurve;
import com.anand.analytics.isdamodel.domain.TInstrumentType;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.utils.DayCount;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by anand on 12/27/14.
 */
public class IRCurveBuilder {
    public static TCurve buildIRZeroCurve(
            LocalDate valueDate,
            char[] instrumentNames,
            LocalDate[] dates,
            double[] rates,
            DayCount mmDCC,
            long fixedSwapFreq,
            long floatSwapFreq,
            DayCount fixedSwapDayCount,
            DayCount floatSwapDayCount,
            TBadDayConvention badDayConvention,
            HolidayCalendar calendar
    ) throws CdsLibraryException {
        final List<LocalDate> cashDates = new ArrayList<LocalDate>();
        final List<LocalDate> swapDates = new ArrayList<LocalDate>();

        final List<Double> cashRates = new ArrayList<Double>();
        final List<Double> swapRates = new ArrayList<>();

        final List<String> validInstruments = TInstrumentType.getValuesAsList();

        for (int i = 0; i < instrumentNames.length; i++) {
            String instr = String.valueOf(instrumentNames[i]).toUpperCase();
            if (!validInstruments.contains(instr))
                throw new CdsLibraryException("Invalid InstrumentType");
            TInstrumentType instrumentType = TInstrumentType.getInstrumentType(instr);
            switch(instrumentType) {
                case M:
                    cashDates.add(dates[i]);
                    cashRates.add(rates[i]);
                    break;
                case S:
                    swapDates.add(dates[i]);
                    swapRates.add(rates[i]);
                    break;
                default:
                    throw new CdsLibraryException("Unknown instrument type");
            }
        }

        final LocalDate[] cashDatesArr = cashDates.toArray(new LocalDate[0]);
        final LocalDate[] swapDatesArr = swapDates.toArray(new LocalDate[0]);

        final double[] cashRatesArr = ArrayUtils.toPrimitive( cashRates.toArray(new Double[0]));
        final double[] swapRatesArr = ArrayUtils.toPrimitive( swapRates.toArray(new Double[0]));



        return null;
    }

}
