package com.anand.analytics.isdamodel.domain;

/**
 * Created by anand on 12/31/14.
 */
public enum TStubPos {
    DEFAULT_AUTO(0), DEFAULT_FRONT(1), DEFAULT_BACK(2);

    private int pos;
    private TStubPos(int i) {
        this.pos = i;
    }

    public int getPos() {
        return pos;
    }
}
