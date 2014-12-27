package com.anand.analytics.isdamodel.cds;


import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.utils.*;
import org.apache.log4j.Logger;
import org.boris.xlloop.util.Day;
import org.boris.xlloop.util.ExcelDate;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Anand on 10/27/2014.
 */
public class ExcelFunctions {

    public final static TStubMethod DEFAULT_STUB_METHOD = new TStubMethod(false, false);
    public final static DayCount DEFAULT_DAY_COUNT = DayCount.ACT_360;
    public final static TBadDayConvention DEFAULT_BAD_DAY_CONVENTION = TBadDayConvention.FOLLOW;
    public final static String DEFAULT_HOLIDAY_CALENDAR = "None";

    private final static Logger logger = Logger.getLogger(ExcelFunctions.class);
    public static ReturnStatus cdsDateIntervalToFreq(final TDateInterval interval, DoubleHolder freq) {
        DoubleHolder years = new DoubleHolder();
        if (interval.getIntervalInYears(years).equals(ReturnStatus.FAILURE))
            return ReturnStatus.FAILURE;

        if (years.get() > 0.)
        {
            freq.set(1. / years.get());
            return ReturnStatus.SUCCESS;
        }

        logger.error("interval is zero");
        return ReturnStatus.FAILURE;
    }

    public static LocalDate xlDateToLocalDateTime(final double anXLDate) {
        Day aDay = Day.fromExcelDate(anXLDate);
        LocalDate localDate = LocalDate.of(aDay.getYear(), aDay.getMonth(), aDay.getDay());
        return localDate;
    }

    public static Boolean[] xlIntsToBooleanArray(final int[] anXLIntArray) {
        /**
         * 0 - false
         * 1 - true
         */
        Boolean[] returnArray =  new Boolean[anXLIntArray.length];
        for (int i = 0; i < returnArray.length; i++) {
            returnArray[i] = anXLIntArray[i] == 0 ? false : true;
        }

        return returnArray;
    }

    public static Boolean[] xlDoublesToBooleanArray(final double[] anXLIntArray) {
        /**
         * 0 - false
         * 1 - true
         */
        Boolean[] returnArray =  new Boolean[anXLIntArray.length];
        for (int i = 0; i < returnArray.length; i++) {
            returnArray[i] = anXLIntArray[i] == 0 ? false : true;
        }

        return returnArray;
    }

    public static LocalDate[] xlDatesToLocalDateTimeArray(final double[] anXlDateArray) {
        LocalDate[] dates = new LocalDate[anXlDateArray.length];
        for (int i = 0; i < anXlDateArray.length; i++) {
            Day aDay = Day.fromExcelDate(anXlDateArray[i]);
            LocalDate localDate = LocalDate.of(aDay.getYear(), aDay.getMonth(), aDay.getDay());
            dates[i] = localDate;
        }

        return dates;
    }

    public static DayCountBasis xlIntToDayCountBasis(final int xlBasis) {
        switch (xlBasis) {
            case 0:
                return DayCountBasis.SIMPLE_BASIS;
            case 1:
                return DayCountBasis.ANNUAL_BASIS;
            case 512:
                return DayCountBasis.DISCOUNT_RATE;
            case 5000:
                return DayCountBasis.CONTINUOUS_BASIS;
            case -2:
                return DayCountBasis.DISCOUNT_FACTOR;
            default:
                return DayCountBasis.ANNUAL_BASIS;
        }
    }

    public static TDateInterval cdsStringToDateInterval(final String dateIntervalString) throws Exception {

        Pattern pattern = Pattern.compile("^([-+]{0,1})(\\d*)(\\w{1})");
        Matcher matcher = pattern.matcher(dateIntervalString);
        if (!(matcher.matches()))
            throw new CdsLibraryException("StringToDateInterval::Invalid date interval string");

        //check the first char
        char[] symbols = new char[]{'+', '-'};
        Character symbol = null;
        int exact = Arrays.binarySearch(symbols, dateIntervalString.charAt(0));
        if (exact >= 0)
            symbol = dateIntervalString.charAt(0);

        //Copy digits if any
        List<Integer> intList = new ArrayList<Integer>();
        int i;
        if (exact >= 0) {
            //start at 1
            for (i = 1; i < dateIntervalString.length(); i++) {
                Character idx = dateIntervalString.charAt(i);
                if (Character.isDigit(idx)) {
                    intList.add(Character.getNumericValue(idx));
                } else {
                    break;
                }
            }
        } else {
            for (i = 0; i < dateIntervalString.length(); i++) {
                char idx = dateIntervalString.charAt(i);
                if (Character.isDigit(idx)) {
                    intList.add((int) idx);
                } else {
                    break;
                }
            }
        }

        List<Character> periodList = new ArrayList<Character>();

        //Now copy the period
        while (i < dateIntervalString.length()) {
            char idx = dateIntervalString.charAt(i);
            periodList.add(idx);
            i++;
        }

        assert (periodList.size() == 1);

        int number = 0;
        if (intList.size() > 0) {

            for (int j = 0; j < intList.size(); j++) {
                number += intList.get(0) * Math.pow(10, j);
            }
        } else {
            number = 1;
        }

        if (exact >= 0) {
            //signed period
            if (symbol.equals('-'))
                number *= -1;
        }

        TDateInterval tDateInterval = TDateInterval.get(number, periodList.get(0));
        return tDateInterval;
    }

    public static TBadDayConvention cdsStringToBadDayConv(final String badDayConvString) {
        return TBadDayConvention.get(badDayConvString.charAt(0));
    }

    public static DayCount cdsStringToDayCountConv(final String dayCountString) {
        String[] components = dayCountString.split("/");
        DayCount dayCount;
        assert (components.length == 2);

        switch (components[0]) {
            case "ACT": {
                switch (components[1]) {
                    case "360":
                        dayCount = DayCount.ACT_360;
                        break;
                    case "365":
                        dayCount = DayCount.ACT_365;
                        break;
                    case "365F":
                        dayCount = DayCount.ACT_365F;
                        break;
                    case "ACT":
                        dayCount = DayCount.ACT_ACT;
                        break;
                    default:
                        dayCount = DayCount.ACT_360;
                }
                break;

            }
            case "B30":
                dayCount = DayCount.B30_360;
                break;
            case "B30E":
                dayCount = DayCount.B30E_360;
                break;
            case "EFF":
                dayCount = DayCount.CDS_EFFECTIVE_RATE;
                break;

            default:
                dayCount = DayCount.ACT_360;

        }

        return dayCount;
    }

    public static TStubMethod cdsStringToStubMethod(final String name) {
        TStubMethod stubMethod = new TStubMethod();

        if (name.contains("/")) {
            String[] components = name.split("/");
            assert (components.length == 2);
            assert (components[0].length() == 1);
            assert (components[1].length() == 1);

            char first = components[0].toUpperCase().charAt(0);
            char second = components[1].toUpperCase().charAt(0);

            switch (first) {
                case 'F':
                    stubMethod.stubAtEnd = false;
                    break;
                case 'B':
                    stubMethod.stubAtEnd = true;
                    break;
                default:
                    stubMethod.stubAtEnd = false;
            }

            switch (second) {
                case 'S':
                    stubMethod.longStub = false;
                    break;
                case 'L':
                    stubMethod.longStub = true;
                    break;
                default:
                    stubMethod.longStub = false;
            }

            return stubMethod;
        } else {
            switch(name) {
                case "F":
                    stubMethod.stubAtEnd = false;
                    break;
                case "B":
                    stubMethod.stubAtEnd = true;
            }

            return stubMethod;
        }
    }

    public static double localDateToExcelDate(LocalDate date) {
        double xlDate = ExcelDate.date(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        return xlDate;
    }

    public static double[] localDateArrayToExcelDateArray(LocalDate[] dates) {
        double[] dateArray = new double[dates.length];
        int i = 0;
        for (LocalDate date : dates) {
            double xlDate = localDateToExcelDate(date);
            dateArray[i++] = xlDate;

        }
        return dateArray;
    }

    public static LocalDate MAX_DATE(LocalDate date1, LocalDate date2) {
        return date1.isAfter(date2) ? date1 : date2;
    }
}
