package com.anand.analytics.isdamodel.utils;

import java.util.List;

/**
 * Created by Anand on 1/18/2015.
 */
public class TwodDoubleArrayReturnType {
    public String returnType; //SUCCESS; FAILURE
    public String errorString;
    public List<double[]> values;

    @Override
    public String toString() {
        return "DfReturnType{" +
                "returnType='" + returnType + '\'' +
                ", errorString='" + errorString + '\'' +
                ", value=" + values +
                '}';
    }
}
