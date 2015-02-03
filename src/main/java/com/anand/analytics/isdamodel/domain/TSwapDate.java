package com.anand.analytics.isdamodel.domain;


import com.anand.analytics.isdamodel.date.Day;

/**
 * Created by anand on 12/30/14.
 */
public class TSwapDate {
    Day adjustedDate;
    Day  originalDate;
    Day previousDate;
    boolean onCycle;

    public Day getAdjustedDate() {
        return adjustedDate;
    }

    public void setAdjustedDate(Day adjustedDate) {
        this.adjustedDate = adjustedDate;
    }

    public Day getOriginalDate() {
        return originalDate;
    }

    public void setOriginalDate(Day originalDate) {
        this.originalDate = originalDate;
    }

    public Day getPreviousDate() {
        return previousDate;
    }

    public void setPreviousDate(Day previousDate) {
        this.previousDate = previousDate;
    }

    public boolean isOnCycle() {
        return onCycle;
    }

    public void setOnCycle(boolean onCycle) {
        this.onCycle = onCycle;
    }
}
