package com.anand.analytics.isdamodel.cds.xml;

import com.anand.analytics.isdamodel.utils.DayCount;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by Anand on 1/14/2015.
 */
public class DayCountXmlAdapter extends XmlAdapter<Integer, DayCount> {
    @Override
    public DayCount unmarshal(Integer v) throws Exception {
        return DayCount.get(v.intValue());
    }

    @Override
    public Integer marshal(DayCount v) throws Exception {
        return v.getValue();
    }
}
