package com.anand.analytics.isdamodel.money;

/**
 * Created by anand on 12/27/14.
 */
public class Currency {
    final String name;
    final String isoCode;
    final String country;
    final int ccyCode;

    public Currency(String name, String isoCode, String country, int code) {
        this.name = name;
        this.isoCode = isoCode;
        this.country = country;
        this.ccyCode = code;
    }
}
