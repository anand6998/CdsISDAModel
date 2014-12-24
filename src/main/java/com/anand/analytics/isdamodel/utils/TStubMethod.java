package com.anand.analytics.isdamodel.utils;

/**
 * Created by aanand on 10/21/2014.
 */
public class TStubMethod {
    public boolean stubAtEnd;
    public boolean longStub;

    public TStubMethod() {
    }

    public TStubMethod(boolean stubAtEnd, boolean longStub) {
        this.stubAtEnd = stubAtEnd;
        this.longStub = longStub;
    }

    @Override
    public String toString() {
        return "TStubMethod{" +
                "stubAtEnd=" + stubAtEnd +
                ", longStub=" + longStub +
                '}';
    }
}
