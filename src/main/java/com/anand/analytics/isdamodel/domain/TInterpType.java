package com.anand.analytics.isdamodel.domain;

/**
 * Created by anand on 12/31/14.
 */
public enum TInterpType {
    LINEAR_FORWARDS(123), FLAT_FORWARDS(124), LINEAR_INTERP(0);

    private int val;
    private TInterpType(int i) { val  = i; }
}
