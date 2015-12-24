package com.anand.analytics.isdamodel.utils;

import com.anand.analytics.isdamodel.domain.TCurve;

/**
 * Created by Anand on 1/18/2015.
 */
public class CdsDatesAndRatesParameters {

    public final TCurve curve;
    public final String user;

    public CdsDatesAndRatesParameters(TCurve curve, String user) {
        this.curve = curve;
        this.user = user;
    }
}
