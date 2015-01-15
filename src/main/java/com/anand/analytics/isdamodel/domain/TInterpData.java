package com.anand.analytics.isdamodel.domain;

import com.anand.analytics.isdamodel.utils.DayCount;
import com.anand.analytics.isdamodel.utils.DayCountBasis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anand on 12/31/14.
 */
public class TInterpData {
    TDateInterval interval;
    DayCount dayCountConv;
    DayCountBasis basis;
    TBadDayList badDayList;
    List<TDateInterval> addPointIntvls = new ArrayList<>(5);
    boolean enableGeneration;

    public TDateInterval getInterval() {
        return interval;
    }

    public void setInterval(TDateInterval interval) {
        this.interval = interval;
    }

    public DayCount getDayCountConv() {
        return dayCountConv;
    }

    public void setDayCountConv(DayCount dayCountConv) {
        this.dayCountConv = dayCountConv;
    }

    public DayCountBasis getBasis() {
        return basis;
    }

    public void setBasis(DayCountBasis basis) {
        this.basis = basis;
    }

    public TBadDayList getBadDayList() {
        return badDayList;
    }

    public void setBadDayList(TBadDayList badDayList) {
        this.badDayList = badDayList;
    }

    public TDateInterval[] getAddPointIntvls() {
        return addPointIntvls.toArray(new TDateInterval[0]);
    }

    public void addInterval(TDateInterval tDateInterval) {
        this.addPointIntvls.add(tDateInterval);
    }

    public boolean isEnableGeneration() {
        return enableGeneration;
    }

    public void setEnableGeneration(boolean enableGeneration) {
        this.enableGeneration = enableGeneration;
    }
}
