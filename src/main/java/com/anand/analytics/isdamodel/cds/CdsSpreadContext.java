package com.anand.analytics.isdamodel.cds;


import com.anand.analytics.isdamodel.utils.DayCount;
import com.anand.analytics.isdamodel.utils.TBadDayConvention;
import com.anand.analytics.isdamodel.utils.TDateInterval;
import com.anand.analytics.isdamodel.utils.TStubMethod;
import org.threeten.bp.LocalDate;

/**
 * Created by Anand on 10/21/2014.
 */
public class CdsSpreadContext {
    LocalDate today;
    LocalDate valueDate;
    LocalDate benchmarkStartDate; /* start date of benchmark CDS for
                                        ** internal clean spread bootstrapping */
    LocalDate stepinDate;
    LocalDate startDate;
    LocalDate endDate;
    double couponRate;
    boolean payAccruedOnDefault;
    TDateInterval dateInterval;
    TStubMethod stubType;
    DayCount accrueDCC;
    TBadDayConvention badDayConv;
    String calendar;
    TCurve discCurve;
    double upfrontCharge;
    double recoveryRate;
    boolean payAccruedAtStart;


}
