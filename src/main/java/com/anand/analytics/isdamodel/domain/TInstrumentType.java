package com.anand.analytics.isdamodel.domain;

import com.anand.analytics.isdamodel.exception.CdsLibraryException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anand on 12/29/14.
 */
public enum TInstrumentType {
    M, S;

    public static List<String> getValuesAsList() {
        List<String> retList = new ArrayList<>(TInstrumentType.values().length);
        for (int i = 0; i < TInstrumentType.values().length; i++)
            retList.add(TInstrumentType.values()[i].name());

        return retList;
    }

    public static TInstrumentType getInstrumentType(String str) throws CdsLibraryException {
        String lStr = str.toLowerCase();
        switch(str) {
            case "m":
                return M;
            case "s":
                return S;
            default:
                throw new CdsLibraryException("Unknown instrument type");
        }
    }

}
