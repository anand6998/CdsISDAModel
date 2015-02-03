package com.anand.analytics.isdamodel.cds;

import com.anand.analytics.isdamodel.date.Day;
import org.junit.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

/**
 * Created by Anand on 1/16/2015.
 */
public class TestDateFunctions {
    @Test
    public void testDay() {
        int numDays = 1000000;
        long startTime = System.currentTimeMillis();
        Day day = new Day(2015, 1, 1);
        System.out.println(day);
        for (int i = 0; i < numDays; i++)
            day = day.addDays(1);

        System.out.println(day);
        long endTime = System.currentTimeMillis();

        System.out.println("Time : " + (endTime - startTime));
        System.out.println();

        Day localDate = new Day(2015, 1, 1);
        System.out.println(localDate);

        startTime = System.currentTimeMillis();
        for (int i = 0; i < numDays; i++)
            localDate = localDate.plusDays(1);

        System.out.println(localDate);
        endTime = System.currentTimeMillis();

        System.out.println("Time : " + (endTime - startTime));
    }

    @Test
    public void testPlusMonths() {
        LocalDate localDate = LocalDate.of(2015, 1, 1);
        System.out.println(localDate);

        localDate = localDate.plusMonths(2);
        System.out.println(localDate);
        System.out.println("");
        Day day = new Day(2015, 1, 1);
        System.out.println(day.addMonths(2));
    }

    @Test
    public void testDayOfWeek() {
        Day day = new Day();
        System.out.println(day);

        for (int i = 0; i < 10; i++) {
            System.out.println(day + "->" + day.getDayOfWeek());
            day.inc();
        }
    }

    @Test
    public void testPeriodUntil() {

        Day day1 = new Day(2015, 1, 1);
        LocalDate localDate1 = LocalDate.of(2015, 1, 1);

        Day day2 = day1.addMonths(2);
        LocalDate localDate2 = localDate1.plusMonths(2);

        System.out.println(day2);
        System.out.println(localDate2);


        System.out.println();
        System.out.println(day2.getDaysBetween(day1));
        System.out.println(localDate1.periodUntil(localDate2, ChronoUnit.DAYS));
    }
}
