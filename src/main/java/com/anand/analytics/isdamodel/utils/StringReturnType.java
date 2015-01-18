package com.anand.analytics.isdamodel.utils;

/**
 * Created by Anand on 1/18/2015.
 */
public class StringReturnType {
    public String returnType; //SUCCESS; FAILURE
    public String errorString;
    public String value;

    @Override
    public String toString() {
        return "DfReturnType{" +
                "returnType='" + returnType + '\'' +
                ", errorString='" + errorString + '\'' +
                ", value=" + value +
                '}';
    }
}

