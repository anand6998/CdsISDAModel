package com.anand.analytics.isdamodel.cds;

import org.threeten.bp.LocalDate;

/**
 * Created by aanand on 12/4/2014.
 */
public class TFeeLegCashFlow {
    LocalDate date;
    double amount;

    public TFeeLegCashFlow(LocalDate date, double amount) {
        this.date = date;
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
