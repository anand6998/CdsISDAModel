package com.anand.analytics.isdamodel.domain;

import com.anand.analytics.isdamodel.date.Day;

/**
 * Created by Anand on 12/4/2014.
 */
public class TFeeLegCashFlow {
    Day date;
    double amount;

    public TFeeLegCashFlow(Day date, double amount) {
        this.date = date;
        this.amount = amount;
    }

    public Day getDate() {
        return date;
    }

    public void setDate(Day date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
