package com.anand.analytics.isdamodel.utils;

import com.anand.analytics.isdamodel.context.XlServerSpringUtils;
import com.anand.analytics.isdamodel.date.HolidayCalendar;
import com.anand.analytics.isdamodel.date.HolidayCalendarFactory;
import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import com.anand.analytics.isdamodel.domain.TCurve;
import com.anand.analytics.isdamodel.domain.TDateInterval;
import com.anand.analytics.isdamodel.domain.TStubMethod;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.grid.GridManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.gridgain.grid.GridException;
import org.threeten.bp.LocalDate;

/**
 * Created by Anand on 1/18/2015.
 */
public class Converter {
    public static LocalDate extractDateFromDouble(JsonObject jsonObject, String fieldName) {
        double xlDate = jsonObject.get(fieldName).getAsDouble();
        LocalDate localDate = ExcelFunctions.xlDateToLocalDateTime(xlDate);
        return localDate;
    }

    public static double extractDouble(JsonObject jsonObject, String fieldName) {
        return jsonObject.get(fieldName).getAsDouble();
    }

    public static LocalDate[] extractDatesFromDoubles(JsonObject jsonObject, String fieldName) {
        JsonArray xlDates = jsonObject.get(fieldName).getAsJsonArray();
        LocalDate dates[] = new LocalDate[xlDates.size()];
        for (int i = 0; i < xlDates.size(); i++)
            dates[i] = ExcelFunctions.xlDateToLocalDateTime(xlDates.get(i).getAsDouble());
        return dates;
    }

    public static double[] extractDoubles(JsonObject jsonObject, String fieldName) {
        JsonArray jsonArray = jsonObject.get(fieldName).getAsJsonArray();
        double[] dbls = new double[jsonArray.size()];

        for (int i = 0; i < jsonArray.size(); i++)
            dbls[i] = jsonArray.get(i).getAsDouble();

        return dbls;
    }

    public static int[] extractInts(JsonObject jsonObject, String fieldName) {
        JsonArray jsonArray = jsonObject.get(fieldName).getAsJsonArray();
        int[] ints = new int[jsonArray.size()];

        for (int i = 0; i < jsonArray.size(); i++)
            ints[i] = jsonArray.get(i).getAsInt();

        return ints;
    }

    public static boolean[] extractBooleansFromInts(JsonObject jsonObject, String fieldName) {
        int[] ints = extractInts(jsonObject, fieldName);
        boolean[] bools = new boolean[ints.length];

        for (int i = 0; i < ints.length; i++) {
            if (ints[i] == 1)
                bools[i] = true;
        }

        return bools;
    }


    public static int extractInt(JsonObject jsonObject, String fieldName) {
        return jsonObject.get(fieldName).getAsInt();
    }

    public static TDateInterval extractDateInterval(JsonObject jsonObject, String fieldName) throws CdsLibraryException {
        return ExcelFunctions.cdsStringToDateInterval(extractString(jsonObject, fieldName));
    }

    public static TStubMethod extractStubType(JsonObject jsonObject, String fieldName) throws CdsLibraryException {
        return ExcelFunctions.cdsStringToStubMethod(extractString(jsonObject, fieldName));
    }

    public static DayCount extractDayCount(JsonObject jsonObject, String fieldName) throws CdsLibraryException {
        return ExcelFunctions.cdsStringToDayCountConv(extractString(jsonObject, fieldName));
    }

    public static TBadDayConvention extractBadDayConvention(JsonObject jsonObject, String fieldName) {
        return ExcelFunctions.cdsStringToBadDayConv(extractString(jsonObject, fieldName));
    }

    public static HolidayCalendar extractHolidayCalendar(JsonObject jsonObject, String fieldName) throws CdsLibraryException {

        HolidayCalendarFactory holidayCalendarFactory
                = (HolidayCalendarFactory) XlServerSpringUtils.getBeanByName("holidayCalendarFactory");
        HolidayCalendar holidayCalendar = holidayCalendarFactory.getCalendar(extractString(jsonObject, fieldName));
        return holidayCalendar;

    }

    public static TCurve extractCurveHandle(JsonObject jsonObject, String fieldName) throws GridException {
        GridManager gridManager = (GridManager) XlServerSpringUtils.getBeanByName("gridManager");
        TCurve tCurve = gridManager.get(extractString(jsonObject, fieldName));
        return tCurve;
    }

    public static String extractString(JsonObject jsonObject, String fieldName) {
        return jsonObject.get(fieldName).getAsString();
    }

    public static boolean extractBooleanFromInt(JsonObject jsonObject, String fieldName) {
        int v = jsonObject.get(fieldName).getAsInt();
        return v == 1 ? true : false;
    }
}
