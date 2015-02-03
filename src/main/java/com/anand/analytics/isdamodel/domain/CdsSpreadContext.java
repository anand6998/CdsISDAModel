package com.anand.analytics.isdamodel.domain;


import com.anand.analytics.isdamodel.date.Day;
import com.anand.analytics.isdamodel.utils.DayCount;

/**
 * Created by Anand on 10/21/2014.
 */
public class CdsSpreadContext {
    public Day today;
    public Day valueDate;
    public Day benchmarkStartDate; /* start date of benchmark CDS for
                                        ** internal clean spread bootstrapping */
    public Day stepinDate;
    public Day startDate;
    public Day endDate;
    public double couponRate;
    public boolean payAccruedOnDefault;
    public TDateInterval dateInterval;
    public TStubMethod stubType;
    public DayCount accrueDCC;
    public TBadDayConvention badDayConv;
    public String calendar;
    public TCurve discCurve;
    public double upfrontCharge;
    public double recoveryRate;
    public boolean payAccruedAtStart;


}
