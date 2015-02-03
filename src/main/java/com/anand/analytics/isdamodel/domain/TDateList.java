package com.anand.analytics.isdamodel.domain;

import com.anand.analytics.isdamodel.date.Day;

import java.util.List;

/**
 * Created by Anand on 10/23/2014.
 */
public class TDateList implements Cloneable {
    int fNumItems;
    Day[] dateArray;

    public TDateList(int numItems) {
        this.fNumItems = numItems;
        this.dateArray = new Day[fNumItems];
    }

    public TDateList() {
        this.fNumItems = 0;
        this.dateArray = null;
    }

    public TDateList(int numItems, Day[] array) {
        this.fNumItems = numItems;
        dateArray = new Day[fNumItems];
        for (int i = 0; i < numItems; i++) {
            this.dateArray[i] = array[i];
        }
    }

    public TDateList(TCurve curve) {
        this.fNumItems = curve.dates.length;
        dateArray = new Day[fNumItems];
        for (int i = 0; i < fNumItems; i++)
            this.dateArray[i] = curve.dates[i];
    }

    public TDateList(List<Day> list) {
        this.fNumItems = list.size();
        dateArray = new Day[fNumItems];
        int i = 0;
        for (Day date : list)
            dateArray[i++] = date;
    }

    public TDateList clone() {
        TDateList list = new TDateList(fNumItems);
        for (int i = 0; i < fNumItems; i++) {
            list.dateArray[i] = dateArray[i];
        }

        return list;
    }

    public TDateList(Day[] array) {
        this.dateArray = array;
        this.fNumItems = array.length;
    }
}
