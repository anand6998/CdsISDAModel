package com.anand.analytics.isdamodel.utils;

import com.anand.analytics.isdamodel.domain.TContingentLeg;
import com.anand.analytics.isdamodel.domain.TCurve;
import com.anand.analytics.isdamodel.domain.TFeeLeg;
import org.threeten.bp.LocalDate;

/**
 * Created by Anand on 10/21/2014.
 */
public class CdsBootstrapContext {
    public int i;
    public LocalDate stepinDate;
    public LocalDate cashSettleDate;
    public TCurve discCurve;
    public TCurve cdsCurve;
    public double recoveryRate;
    public TContingentLeg contigentLeg;
    public TFeeLeg feeLeg;

    public CdsBootstrapContext(int i, LocalDate stepinDate, LocalDate cashSettleDate, TCurve discCurve, TCurve cdsCurve, double recoveryRate, TContingentLeg contigentLeg, TFeeLeg feeLeg) {

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
