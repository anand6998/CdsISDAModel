package com.anand.analytics.isdamodel.cds.xml;

import com.anand.analytics.isdamodel.date.Day;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by Anand on 1/14/2015.
 */
public class LocalDateXmlAdapter extends XmlAdapter<String, Day> {

    final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Override
    public Day unmarshal(String v) throws Exception {
        LocalDate d = (LocalDate) formatter.parse(v);
        return new Day(d.getYear(), d.getMonthValue(), d.getDayOfMonth());
    }

    @Override
    public String marshal(Day v) throws Exception {
        LocalDate d = LocalDate.of(v.getYear(), v.getMonth(), v.getDay());
        return formatter.format(d);
    }
}
