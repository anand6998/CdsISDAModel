package com.anand.analytics.isdamodel.utils;

import com.anand.analytics.isdamodel.domain.TContingentLeg;
import com.anand.analytics.isdamodel.domain.TCurve;
import com.anand.analytics.isdamodel.domain.TFeeLeg;
import com.anand.analytics.isdamodel.date.Day;

/**
 * Created by Anand on 10/21/2014.
 */
public class CdsBootstrapContext {
    public int i;
    public Day stepinDate;
    public Day cashSettleDate;
    public TCurve discCurve;
    public TCurve cdsCurve;
    public double recoveryRate;
    public TContingentLeg contigentLeg;
    public TFeeLeg feeLeg;

    public CdsBootstrapContext(int i, Day stepinDate, Day cashSettleDate, TCurve discCurve, TCurve cdsCurve, double recoveryRate, TContingentLeg contigentLeg, TFeeLeg feeLeg) {

        this.i = i;
        this.stepinDate = stepinDate;
        this.cashSettleDate = cashSettleDate;
        this.discCurve = discCurve;
        this.cdsCurve = cdsCurve;
        this.recoveryRate = recoveryRate;
        this.contigentLeg = contigentLeg;
        this.feeLeg = feeLeg;
    }

    public CdsBootstrapContext() {
    }
}
