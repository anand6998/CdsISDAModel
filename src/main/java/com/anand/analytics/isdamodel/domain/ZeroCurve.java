package com.anand.analytics.isdamodel.domain;

import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.utils.*;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anand on 12/29/14.
 */
public class ZeroCurve {
    private final static Logger logger = Logger.getLogger(ZeroCurve.class);

    private LocalDate valueDate;
    private List<Double> rates;
    private List<LocalDate> dates;
    private List<Double> discs;

    private DayCountBasis dayCountBasis;
    private DayCount dayCount;

    public ZeroCurve(LocalDate valueDate, DayCountBasis dayCountBasis, DayCount dayCount) {
        this.valueDate = valueDate;
        this.dayCountBasis = dayCountBasis;
        this.dayCount = dayCount;


        rates = new ArrayList<>();
        dates = new ArrayList<>();
        discs = new ArrayList<>();
    }

    public ZeroCurve(TCurve curve) {
        this(curve.getBaseDate(), curve.getBasis(), curve.getDayCountConv());
    }

    public void addRate(LocalDate date, double rate, double disc) throws CdsLibraryException {
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
            ReturnStatus status = CdsUtils.binarySearchLong(date, dates.toArray(new LocalDate[0]), exact, loBound, hiBound);

            if (status.equals(ReturnStatus.FAILURE))
                throw new CdsLibraryException("Failed locating index for date");

            List<LocalDate> newDates = new ArrayList<>();
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

    private double computeDiscount(LocalDate date, double rate) throws CdsLibraryException {
        if (this.dayCountBasis.equals(DayCountBasis.ANNUAL_BASIS) &&
                rate >= -1.0 &&
                date.isAfter(valueDate) &&
                (this.dayCount.equals(DayCount.ACT_365F) || this.dayCount.equals(DayCount.ACT_360) )) {
            double discount = Math.pow( 1 + rate, (valueDate.periodUntil(date, ChronoUnit.DAYS) * -1) / (dayCount.equals(DayCount.ACT_360) ? 360. : 365. ));
            return discount;
        }

        double df = cdsRateToDiscount(rate, valueDate, date, dayCount, dayCountBasis);
        return df;
    }

    private double cdsRateToDiscount(double rate, LocalDate startDate, LocalDate endDate, DayCount rateDayCountConv, DayCountBasis rateBasis)
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
