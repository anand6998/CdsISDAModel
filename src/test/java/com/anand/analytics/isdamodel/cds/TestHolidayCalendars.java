package com.anand.analytics.isdamodel.cds;

import com.anand.analytics.isdamodel.context.XlServerSpringUtils;
import com.anand.analytics.isdamodel.date.Day;
import com.anand.analytics.isdamodel.date.HolidayCalendar;
import com.anand.analytics.isdamodel.date.HolidayCalendarFactory;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by anand on 12/26/14.
 */
public class TestHolidayCalendars {
    @Test
    public void testHolidayCalendars() throws Exception {
        HolidayCalendarFactory calendarFactory = (HolidayCalendarFactory) XlServerSpringUtils.getBeanByName("holidayCalendarFactory");
        HolidayCalendar noHolidayCalendar = calendarFactory.getCalendar("None");
        HolidayCalendar usHolidayCalendar = calendarFactory.getCalendar("USD");

        Day todaysDate = new Day(2014, 12, 20);
        Day noHolidayCalendarDate = noHolidayCalendar.addBusinessDays(todaysDate, 20);
        Day usHolidayCalendarDate = usHolidayCalendar.addBusinessDays(todaysDate, 20);

        Assert.assertEquals(new Day(2015, 1, 16), noHolidayCalendarDate );
        Assert.assertEquals(new Day(2015, 1, 21), usHolidayCalendarDate);

        System.out.println(noHolidayCalendarDate);
        System.out.println(usHolidayCalendarDate);
    }

    @Test
    public void testMultiHolidayCalendar() throws Exception {
        HolidayCalendarFactory calendarFactory = (HolidayCalendarFactory) XlServerSpringUtils.getBeanByName("holidayCalendarFactory");
        HolidayCalendar usdUaeHolidayCalendar = calendarFactory.getCalendar("USD,UAE");

        Day todaysDate = new Day(2013, 12, 20);
        Day usdUaeHolidayCalendarDate = usdUaeHolidayCalendar.addBusinessDays(todaysDate, 20);

        System.out.println(usdUaeHolidayCalendarDate);
    }
}
