package com.anand.analytics.isdamodel.domain;


import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.utils.*;
import org.apache.log4j.Logger;
import com.anand.analytics.isdamodel.date.Day;

import java.io.Serializable;


/**
 * Created by Anand on 10/21/2014.
 */
public class TCurve implements Serializable, Cloneable {


    final static Logger logger = Logger.getLogger(TCurve.class);
    private static final long serialVersionUID = 7528245660223429470L;
    Day baseDate;
    Day[] dates;
    double[] rates;

    DayCountBasis basis;
    DayCount dayCountConv;

    public Day getBaseDate() {
        return baseDate;
    }

    public Day[] getDates() {
        return dates;
    }

    public double[] getRates() {
        return rates;
    }

    public DayCountBasis getBasis() {
        return basis;
    }

    public DayCount getDayCountConv() {
        return dayCountConv;
    }

    private TCurve() {}

    public TCurve(Day baseDate, Day[] dates, double[] rates, DayCountBasis basis, DayCount dayCountConv) throws CdsLibraryException {
        this.baseDate = baseDate;
        this.dates = dates;
        this.rates = rates;
        this.basis = basis;
        this.dayCountConv = dayCountConv;

        check();
    }

    private void check() throws CdsLibraryException {
        if (rates.length != dates.length)
            throw new CdsLibraryException("Rates and dates arrays must be of same length");

        for (int i = 1; i < dates.length; i++) {
            if (dates[i - 1].isAfter(dates[i]))
                throw new CdsLibraryException("Invalid Curve dates");

            if (cdsRateValid(this.rates[i],
                    this.baseDate,
                    this.dates[i],
                    this.dayCountConv,
                    this.basis).equals(ReturnStatus.FAILURE)) {
                throw new CdsLibraryException("Rate implies non-positive discount factor");
            }
        }
    }

    private ReturnStatus cdsRateValid(double rate, Day startDate, Day endDate, DayCount rateDayCountConv, DayCountBasis rateBasis) {
        ReturnStatus status;
        DoubleHolder yearFraction = new DoubleHolder();
        switch (rateBasis) {
            case SIMPLE_BASIS:
            case DISCOUNT_RATE: {
                status = TDateFunctions.cdsDayCountFraction(startDate, endDate, rateDayCountConv, yearFraction);
                if (status.equals(ReturnStatus.FAILURE)) {
                    logger.error("TCurve.cdsRateValid()::Error in calculating day count fraction");
                    return ReturnStatus.FAILURE;
                }
            }
            break;
            default:
                yearFraction.set(1.0);
        }

        if (cdsRateValidYearFrac(rate, yearFraction, rateBasis).equals(ReturnStatus.FAILURE)) {
            logger.error("TCurve.cdsRateValid()::Error in calculating year fraction");
            return ReturnStatus.FAILURE;
        }
        return ReturnStatus.SUCCESS;
    }

    private ReturnStatus cdsRateValidYearFrac(double rate, DoubleHolder yearFraction, DayCountBasis basis) {
        switch (basis) {
            case SIMPLE_BASIS:
                if (rate * yearFraction.get() <= -1.0)
                    return ReturnStatus.FAILURE;
            case DISCOUNT_RATE:
                if (rate * yearFraction.get() >= 1.0)
                    return ReturnStatus.FAILURE;
            case CONTINUOUS_BASIS:
                return ReturnStatus.SUCCESS;
            default:
                if (rate <= -basis.getValue())
                    return ReturnStatus.FAILURE;
        }
        return ReturnStatus.SUCCESS;
    }

    public void setBasis(DayCountBasis basis) {
        this.basis = basis;
    }

    public TCurve clone() {
        TCurve tCurve = new TCurve();
        tCurve.baseDate = this.baseDate;
        tCurve.basis = this.basis;
        tCurve.dayCountConv = this.dayCountConv;

        tCurve.dates = new Day[this.dates.length];
        tCurve.rates = new double[this.rates.length];

        for (int i = 0; i < dates.length; i++) {
            tCurve.dates[i] = this.dates[i];
            tCurve.rates[i] = this.rates[i];
        }

        return tCurve;
    }
}