package com.anand.analytics.isdamodel.cds;

import com.anand.analytics.isdamodel.cds.xml.DayCountBasisXmlAdapter;
import com.anand.analytics.isdamodel.cds.xml.DayCountXmlAdapter;
import com.anand.analytics.isdamodel.cds.xml.LocalDateXmlAdapter;
import com.anand.analytics.isdamodel.utils.DayCount;
import com.anand.analytics.isdamodel.utils.DayCountBasis;
import com.anand.analytics.isdamodel.date.Day;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by Anand on 1/14/2015.
 */
@XmlRootElement
public class TCurveStorable {

    public TCurveStorable() {}
    public TCurveStorable(Day baseDate, Day[] dates, double[] rates, DayCountBasis basis, DayCount dayCountConv) {
        this.baseDate = baseDate;
        this.dates = dates;
        this.rates = rates;
        this.basis = basis;
        this.dayCountConv = dayCountConv;
    }

    @XmlJavaTypeAdapter(type = Day.class, value = LocalDateXmlAdapter.class)
    Day baseDate;

    @XmlElementWrapper(name = "dates")
    @XmlElement(name = "date")
    @XmlJavaTypeAdapter(type = Day.class, value = LocalDateXmlAdapter.class)
    Day[] dates;

    @XmlElementWrapper(name = "rates")
    @XmlElement(name = "rate")
    double[] rates;

    @XmlJavaTypeAdapter(type = DayCountBasis.class, value = DayCountBasisXmlAdapter.class)
    DayCountBasis basis;

    @XmlJavaTypeAdapter(type = DayCount.class, value = DayCountXmlAdapter.class)
    DayCount dayCountConv;

}
