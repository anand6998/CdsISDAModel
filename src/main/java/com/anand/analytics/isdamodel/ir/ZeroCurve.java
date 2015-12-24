package com.anand.analytics.isdamodel.ir;

import com.anand.analytics.isdamodel.date.Day;
import com.anand.analytics.isdamodel.domain.TCurve;
import com.anand.analytics.isdamodel.domain.TDateFunctions;
import com.anand.analytics.isdamodel.domain.TRateFunctions;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.utils.CdsUtils;
import com.anand.analytics.isdamodel.utils.DayCount;
import com.anand.analytics.isdamodel.utils.DayCountBasis;
import com.anand.analytics.isdamodel.utils.DoubleHolder;
import com.anand.analytics.isdamodel.utils.IntHolder;
import com.anand.analytics.isdamodel.utils.ReturnStatus;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anand on 12/29/14.
 */
public class ZeroCurve {
    private final static Logger logger = Logger.getLogger(ZeroCurve.class);

    private Day valueDate;
    private List<Double> rates;
    private List<Day> dates;
    private List<Double> discs;

    private DayCountBasis dayCountBasis;
    private DayCount dayCount;

    private int fNumItems;


    public ZeroCurve(Day valueDate, DayCountBasis dayCountBasis, DayCount dayCount) {
        this.valueDate = valueDate;
        this.dayCountBasis = dayCountBasis;
        this.dayCount = dayCount;

        rates = new ArrayList<>();
        dates = new ArrayList<>();
        discs = new ArrayList<>();
    }

    public Day getValueDate() {
        return valueDate;
    }

    public double[] getRates() {
        return ArrayUtils.toPrimitive(rates.toArray(new Double[0]));
    }

    public Day[] getDates() {
        return (dates.toArray(new Day[0]));
    }

    public double[] getDiscs() {
        return ArrayUtils.toPrimitive(discs.toArray(new Double[0]));
    }

    public DayCountBasis getDayCountBasis() {
        return dayCountBasis;
    }

    public DayCount getDayCount() {
        return dayCount;
    }

    public int getfNumItems() {
        return fNumItems;
    }


    public ZeroCurve(TCurve curve) throws CdsLibraryException {
        this(curve.getBaseDate(), curve.getBasis(), curve.getDayCountConv());
        for (int i = 0; i < curve.getDates().length; i++)
            addRate(curve.getDates()[i], curve.getRates()[i]);

        this.fNumItems = curve.getDates().length;
    }

    public void addRates(Day[] dates, double[] rates, DayCount dayCount) throws CdsLibraryException {
        try {
            Validate.isTrue(dates.length == rates.length, "dates.length != rates.length");
            for (int i = 0; i < dates.length; i++) {
                addGenRate(dates[i], rates[i], DayCountBasis.SIMPLE_BASIS, dayCount);
            }

            this.fNumItems = dates.length;
        } catch (Exception ex) {
            logger.error(ex);
            throw new CdsLibraryException(ex.getMessage());
        }
    }

    private void addGenRate(Day date, double rate, DayCountBasis basis, DayCount dayCount) throws CdsLibraryException {
        if (basis.equals(this.dayCountBasis) && dayCount.equals(this.dayCount)) {
            addRate(date, rate);
            return;
        }

        cdsRateToDiscount(rate, valueDate, date, dayCount, basis);
    }

    private void addRate(Day date, double rate) throws CdsLibraryException {
        double discount = computeDiscount(date, rate);
        addRate(date, rate, discount);
    }
    private void addRate(Day date, double rate, double disc) throws CdsLibraryException {
        /**
         * Make sure date not already in list
         *
         */
        if (dates.contains(date) ) {
            int idx = dates.indexOf(date);
            if ((rate - rates.get(idx)) < 0.0000001)
                return;
            else
                throw new CdsLibraryException("Date already in curve");
        }

        if (dates.size() == 0 || dates.get(dates.size() -1).isBefore(date)) {
            dates.add(date);
            rates.add(rate);
            discs.add(disc);
        } else {
            //find where to insert
            IntHolder exact = new IntHolder(0);
            IntHolder loBound = new IntHolder(0);
            IntHolder hiBound = new IntHolder(0);
            ReturnStatus status = CdsUtils.binarySearchLong(date, dates.toArray(new Day[0]), exact, loBound, hiBound);

            if (status.equals(ReturnStatus.FAILURE))
                throw new CdsLibraryException("Failed locating index for date");

            List<Day> newDates = new ArrayList<>();
            List<Double> newRates = new ArrayList<>();
            List<Double> newDiscs = new ArrayList<>();

            //Copy the low items
            int i = 0;
            for (;i <= loBound.get(); i++) {
                newDates.add(i, dates.get(i));
                newRates.add(i, rates.get(i));
                newDiscs.add(i, discs.get(i));
            }

            //Add the new entry
            newDates.add(i, date);
            newRates.add(i, rate);
            newDiscs.add(i, disc);

            ++i;
            //Copy the hi items
            for (int j = hiBound.get(); j < dates.size(); j++) {
                newDates.add(i, dates.get(j));
                newRates.add(i, rates.get(j));
                newDiscs.add(i, discs.get(j));
                ++i;
            }

            this.dates = newDates;
            this.rates = newRates;
            this.discs = newDiscs;

        }

    }

    public TCurve toTCurve() throws CdsLibraryException {
        try {
            return new TCurve(valueDate, dates.toArray(new Day[0]), ArrayUtils.toPrimitive( rates.toArray(new Double[0])), dayCountBasis, dayCount);
        } catch (Exception e) {
            throw new CdsLibraryException(e.getMessage());
        }
    }

    private double computeDiscount(Day date, double rate) throws CdsLibraryException {
        if (this.dayCountBasis.equals(DayCountBasis.ANNUAL_BASIS) &&
                rate >= -1.0 &&
                date.isAfter(valueDate) &&
                (this.dayCount.equals(DayCount.ACT_365F) || this.dayCount.equals(DayCount.ACT_360) )) {
            double discount = Math.pow( 1 + rate, (valueDate.getDaysBetween(date) * -1) / (dayCount.equals(DayCount.ACT_360) ? 360. : 365. ));
            return discount;
        }

        double df = cdsRateToDiscount(rate, valueDate, date, dayCount, dayCountBasis);
        return df;
    }

    private double cdsRateToDiscount(double rate, Day startDate, Day endDate, DayCount rateDayCountConv, DayCountBasis rateBasis)
    throws CdsLibraryException{
        if (rateBasis.equals(DayCountBasis.DISCOUNT_FACTOR)) {
            if (rate <= 0.0)
                throw new CdsLibraryException("Bad rate");

            return rate;
        }

        if (rateBasis.getValue() < DayCountBasis.SIMPLE_BASIS.getValue())
            throw new CdsLibraryException("Bad dayCount basis");

        DoubleHolder rateYF = new DoubleHolder();
        if (TDateFunctions.cdsDayCountFraction(startDate, endDate, rateDayCountConv, rateYF).equals(ReturnStatus.FAILURE)) {
            logger.error("Error calculating cdsDayCountFraction");
            throw new CdsLibraryException("Error calculating cdsDayCountFraction");
        }

        DoubleHolder df = new DoubleHolder();
        if (TRateFunctions.cdsRateToDiscountYearFrac(rate, rateYF.get(), rateBasis, df).equals(ReturnStatus.FAILURE)) {
            logger.error("Error calculating discount factor");
            throw new CdsLibraryException("Error calculating discount factor");
        }

        return df.get();
    }
}
