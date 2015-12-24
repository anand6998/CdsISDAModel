package com.anand.analytics.isdamodel.utils;

import org.apache.log4j.Logger;

import java.util.UUID;

/**
 * Created by Anand on 1/18/2015.
 */
public class TCurveIdGenerator {
    private final static Logger logger = Logger.getLogger(TCurveIdGenerator.class);
    public static String id(String caller) {

        String curveId = "TCurve@" + UUID.randomUUID();
        return curveId;

    }
}
