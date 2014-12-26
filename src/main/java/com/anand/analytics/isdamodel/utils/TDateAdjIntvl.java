package com.anand.analytics.isdamodel.utils;

/**
 * Created by Anand on 12/25/2014.
 */
public class TDateAdjIntvl {
    final TDateInterval interval;
    final CdsDateAdjType isBusDays;
    final String holidayFile;
    final TBadDayConvention badDayConvention;

    public TDateAdjIntvl(TDateInterval interval, CdsDateAdjType isBusDays, String holidayFile, TBadDayConvention badDayConvention) {
        this.interval = interval;
        this.isBusDays = isBusDays;
        this.holidayFile = holidayFile;
        this.badDayConvention = badDayConvention;
    }

    public TDateInterval getInterval() {
        return interval;
    }

    public CdsDateAdjType getIsBusDays() {
        return isBusDays;
    }

    public String getHolidayFile() {
        return holidayFile;
    }

    public TBadDayConvention getBadDayConvention() {
        return badDayConvention;
    }
}
