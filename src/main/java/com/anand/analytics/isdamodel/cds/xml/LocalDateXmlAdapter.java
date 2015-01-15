package com.anand.analytics.isdamodel.cds.xml;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by Anand on 1/14/2015.
 */
public class LocalDateXmlAdapter extends XmlAdapter<String, LocalDate> {

    final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Override
    public LocalDate unmarshal(String v) throws Exception {
        return (LocalDate) formatter.parse(v);
    }

    @Override
    public String marshal(LocalDate v) throws Exception {
        return formatter.format(v);
    }
}
