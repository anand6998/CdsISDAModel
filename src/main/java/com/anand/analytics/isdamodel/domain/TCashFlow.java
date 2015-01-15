package com.anand.analytics.isdamodel.domain;

import org.threeten.bp.LocalDate;

/**
 * Created by anand on 1/1/15.
 */
public class TCashFlow {
    private LocalDate fDate;
    private double amount;

    public LocalDate getfDate() {
        return fDate;
    }

    public void setfDate(LocalDate fDate) {
        this.fDate = fDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TCashFlow(LocalDate fDate, double amount) {
        this.fDate = fDate;
        this.amount = amount;
    }

    public TCashFlow() {}
}
