package com.anand.analytics.isdamodel.cds;


import com.anand.analytics.isdamodel.utils.DayCount;
import com.anand.analytics.isdamodel.utils.DayCountBasis;
import com.anand.analytics.isdamodel.utils.DoubleHolder;
import com.anand.analytics.isdamodel.utils.ReturnStatus;
import org.apache.log4j.Logger;
import org.threeten.bp.LocalDate;

import static com.anand.analytics.isdamodel.cds.TDateFunctions.cdsDayCountFraction;


/**
 * Created by Anand on 10/21/2014.
 */
public class TCurve {
    final static Logger logger = Logger.getLogger(TCurve.class);
    LocalDate baseDate;
    LocalDate[] dates;
    double[] rates;

    DayCountBasis basis;
    DayCount dayCountConv;

    public LocalDate getBaseDate() {
        return baseDate;
    }

    public LocalDate[] getDates() {
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

    public TCurve(LocalDate baseDate, LocalDate[] dates, double[] rates, DayCountBasis basis, DayCount dayCountConv) throws Exception {
        this.baseDate = baseDate;
        this.dates = dates;
        this.rates = rates;
        this.basis = basis;
        this.dayCountConv = dayCountConv;

        check();
    }

    private void check() throws Exception {
        if (rates.length != dates.length)
            throw new Exception("Rates and dates arrays must be of same length");

        for (int i = 1; i < dates.length; i++) {
            if (dates[i - 1].isAfter(dates[i]))
                throw new Exception("Invalid Curve dates");

            if (cdsRateValid(this.rates[i],
                    this.baseDate,
                    this.dates[i],
                    this.dayCountConv,
                    this.basis).equals(ReturnStatus.FAILURE)) {
                throw new Exception("Rate implies non-positive discount factor");
            }
        }
    }

    private ReturnStatus cdsRateValid(double rate, LocalDate startDate, LocalDate endDate, DayCount rateDayCountConv, DayCountBasis rateBasis) {
        ReturnStatus status;
        DoubleHolder yearFraction = new DoubleHolder();
        switch (rateBasis) {
            case SIMPLE_BASIS:
            case DISCOUNT_RATE: {
                status = cdsDayCountFraction(startDate, endDate, rateDayCountConv, yearFraction);
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
}