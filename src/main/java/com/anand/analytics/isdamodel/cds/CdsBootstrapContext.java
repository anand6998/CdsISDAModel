package com.anand.analytics.isdamodel.cds;

import org.threeten.bp.LocalDate;

/**
 * Created by aanand on 10/21/2014.
 */
public class CdsBootstrapContext {
    int i;
    LocalDate stepinDate;
    LocalDate cashSettleDate;
    TCurve discCurve;
    TCurve cdsCurve;
    double recoveryRate;
    TContingentLeg contigentLeg;
    TFeeLeg feeLeg;

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
