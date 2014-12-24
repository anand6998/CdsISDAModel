package com.anand.analytics.isdamodel.utils;

/**
 * Created by Anand on 10/21/2014.
 */
public interface SolvableFunction {
    public ReturnStatus eval(double x, Object data, DoubleHolder y);
}
