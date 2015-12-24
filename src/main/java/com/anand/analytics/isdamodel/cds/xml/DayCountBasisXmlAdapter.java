package com.anand.analytics.isdamodel.cds.xml;

import com.anand.analytics.isdamodel.utils.DayCountBasis;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by Anand on 1/14/2015.
 */
public class DayCountBasisXmlAdapter extends XmlAdapter<Integer, DayCountBasis> {
    @Override
    public DayCountBasis unmarshal(Integer v) throws Exception {
        return DayCountBasis.get(v.intValue());
    }

    @Override
    public Integer marshal(DayCountBasis v) throws Exception {
        return v.index();
    }
}
