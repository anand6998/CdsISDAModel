package com.anand.analytics.isdamodel.domain;


import org.threeten.bp.LocalDate;

/**
 * Created by anand on 12/30/14.
 */
public class TSwapDate {
    LocalDate adjustedDate;
    LocalDate  originalDate;
    LocalDate previousDate;
    boolean onCycle;

    public LocalDate getAdjustedDate() {
        return adjustedDate;
    }

    public void setAdjustedDate(LocalDate adjustedDate) {
        this.adjustedDate = adjustedDate;
    }

    public LocalDate getOriginalDate() {
        return originalDate;
    }

    public void setOriginalDate(LocalDate originalDate) {
        this.originalDate = originalDate;
    }

    public LocalDate getPreviousDate() {
        return previousDate;
    }

    public void setPreviousDate(LocalDate previousDate) {
        this.previousDate = previousDate;
    }

    public boolean isOnCycle() {
        return onCycle;
    }

    public void setOnCycle(boolean onCycle) {
        this.onCycle = onCycle;
    }
}
