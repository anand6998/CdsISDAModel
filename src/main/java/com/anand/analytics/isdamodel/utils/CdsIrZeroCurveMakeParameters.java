package com.anand.analytics.isdamodel.utils;

import org.threeten.bp.LocalDate;

/**
 * Created by Anand on 1/18/2015.
 */
public class CdsIrZeroCurveMakeParameters {

    public final LocalDate baseDate;
    public final  LocalDate[] dates;
    public final  double[] rates;
    public final  DayCountBasis basis;
    public final  DayCount dayCount;
    public final  String user;


    public CdsIrZeroCurveMakeParameters(LocalDate baseDate,
                                        LocalDate[] dates,
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