package com.anand.analytics.isdamodel.domain;


import com.anand.analytics.isdamodel.utils.DayCount;
import org.threeten.bp.LocalDate;

/**
 * Created by Anand on 10/21/2014.
 */
public class CdsSpreadContext {
    public LocalDate today;
    public LocalDate valueDate;
    public LocalDate benchmarkStartDate; /* start date of benchmark CDS for
                                        ** internal clean spread bootstrapping */
    public LocalDate stepinDate;
    public LocalDate startDate;
    public LocalDate endDate;
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
