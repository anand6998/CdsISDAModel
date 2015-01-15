package com.anand.analytics.isdamodel.domain;

import org.threeten.bp.LocalDate;

import java.util.List;

/**
 * Created by Anand on 10/23/2014.
 */
public class TDateList implements Cloneable {
    int fNumItems;
    LocalDate[] dateArray;

    public TDateList(int numItems) {
        this.fNumItems = numItems;
        this.dateArray = new LocalDate[fNumItems];
    }

    public TDateList() {
        this.fNumItems = 0;
        this.dateArray = null;
    }

    public TDateList(int numItems, LocalDate[] array) {
        this.fNumItems = numItems;
        dateArray = new LocalDate[fNumItems];
        for (int i = 0; i < numItems; i++) {
            this.dateArray[i] = array[i];
        }
    }

    public TDateList(TCurve curve) {
        this.fNumItems = curve.dates.length;
        dateArray = new LocalDate[fNumItems];
        for (int i = 0; i < fNumItems; i++)
            this.dateArray[i] = curve.dates[i];
    }

    public TDateList(List<LocalDate> list) {
        this.fNumItems = list.size();
        dateArray = new LocalDate[fNumItems];
        int i = 0;
        for (LocalDate date : list)
            dateArray[i++] = date;
    }

    public TDateList clone() {
        TDateList list = new TDateList(fNumItems);
        for (int i = 0; i < fNumItems; i++) {
            list.dateArray[i] = dateArray[i];
        }

        return list;
    }

    public TDateList(LocalDate[] array) {
        this.dateArray = array;
        this.fNumItems = array.length;
    }
}
