package com.anand.analytics.isdamodel.date;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anand on 12/27/14.
 */
public class HolidayListReader {
    //yyyy-mm-dd
    private final static DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public List<LocalDate> read(final List<String> holidays) {
        final List<LocalDate> dates = new ArrayList<>(holidays.size());
        for (int i = 0; i < holidays.size(); i++) {
            LocalDate localDate = LocalDate.parse(holidays.get(i), formatter);

            dates.add(localDate);
        }


        return dates;
    }
}
