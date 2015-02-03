package com.anand.analytics.isdamodel.utils;

import com.anand.analytics.isdamodel.date.Day;

/**
 * Created by Anand on 1/18/2015.
 */
public class CdsIrZeroCurveMakeParameters {

    public final Day baseDate;
    public final  Day[] dates;
    public final  double[] rates;
    public final  DayCountBasis basis;
    public final  DayCount dayCount;
    public final  String user;


    public CdsIrZeroCurveMakeParameters(Day baseDate,
                                        Day[] dates,
                                        double[] rates,
                                        DayCountBasis basis,
                                        DayCount dayCount,
                                        String user) {
        this.baseDate = baseDate;
        this.dates = dates;
        this.rates = rates;
        this.basis = basis;
        this.dayCount = dayCount;
        this.user = user;
    }
}