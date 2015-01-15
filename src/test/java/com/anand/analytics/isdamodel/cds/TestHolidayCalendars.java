package com.anand.analytics.isdamodel.cds;

import com.anand.analytics.isdamodel.context.XlServerSpringUtils;
import com.anand.analytics.isdamodel.date.HolidayCalendar;
import com.anand.analytics.isdamodel.date.HolidayCalendarFactory;
import junit.framework.Assert;
import org.junit.Test;
import org.threeten.bp.LocalDate;

/**
 * Created by anand on 12/26/14.
 */
public class TestHolidayCalendars {
    @Test
    public void testHolidayCalendars() throws Exception {
        HolidayCalendarFactory calendarFactory = (HolidayCalendarFactory) XlServerSpringUtils.getBeanByName("holidayCalendarFactory");
        HolidayCalendar noHolidayCalendar = calendarFactory.getCalendar("None");
        HolidayCalendar usHolidayCalendar = calendarFactory.getCalendar("USD");

        LocalDate todaysDate = LocalDate.of(2014, 12, 20);
        LocalDate noHolidayCalendarDate = noHolidayCalendar.addBusinessDays(todaysDate, 20);
        LocalDate usHolidayCalendarDate = usHolidayCalendar.addBusinessDays(todaysDate, 20);

        Assert.assertEquals(LocalDate.of(2015, 1, 16), noHolidayCalendarDate );
        Assert.assertEquals(LocalDate.of(2015, 1, 21), usHolidayCalendarDate);

        System.out.println(noHolidayCalendarDate);
        System.out.println(usHolidayCalendarDate);
    }

    @Test
    public void testMultiHolidayCalendar() throws Exception {
        HolidayCalendarFactory calendarFactory = (HolidayCalendarFactory) XlServerSpringUtils.getBeanByName("holidayCalendarFactory");
        HolidayCalendar usdUaeHolidayCalendar = calendarFactory.getCalendar("USD,UAE");

        LocalDate todaysDate = LocalDate.of(2013, 12, 20);
        LocalDate usdUaeHolidayCalendarDate = usdUaeHolidayCalendar.addBusinessDays(todaysDate, 20);

        System.out.println(usdUaeHolidayCalendarDate);
    }
}
