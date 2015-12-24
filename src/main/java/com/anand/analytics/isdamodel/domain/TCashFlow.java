package com.anand.analytics.isdamodel.domain;

import com.anand.analytics.isdamodel.date.Day;

/**
 * Created by anand on 1/1/15.
 */
public class TCashFlow {
    private Day fDate;
    private double amount;

    public Day getfDate() {
        return fDate;
    }

    public void setfDate(Day fDate) {
        this.fDate = fDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TCashFlow(Day fDate, double amount) {
        this.fDate = fDate;
        this.amount = amount;
    }

    public TCashFlow() {}
}
