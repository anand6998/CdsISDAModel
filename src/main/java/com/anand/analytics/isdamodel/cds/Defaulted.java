package com.anand.analytics.isdamodel.cds;


import com.anand.analytics.isdamodel.domain.*;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.utils.DayCount;
import com.anand.analytics.isdamodel.utils.DoubleHolder;
import com.anand.analytics.isdamodel.utils.ReturnStatus;
import org.apache.log4j.Logger;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

/**
 * Created by Anand on 12/18/2014.
 */
public class Defaulted {
    private static final Logger logger = Logger.getLogger(Defaulted.class);
    public static ReturnStatus cdsDefaultAccrual(LocalDate tradeDate,
                                                 LocalDate edd,
                                                 LocalDate startDate,
                                                 LocalDate endDate,
                                                 TDateInterval couponInterval,
                                                 TStubMethod stubType,
                                                 double notional,
                                                 double couponRate,
                                                 DayCount paymentDcc,
                                                 TBadDayConvention badDayConvention,
                                                 String calendar,
                                                 DoubleHolder accrualDays,
                                                 DoubleHolder defaultAccrual
                                                 ) {
        try {
            if (tradeDate.isBefore(edd))
                throw new CdsLibraryException("Trade date is before edd");

            if (edd.isBefore(startDate))
                throw new CdsLibraryException("edd is before Start Date");

            TFeeLeg feeLeg = new TFeeLeg(startDate, endDate, true,
                    couponInterval, stubType, notional,
                    couponRate, paymentDcc, badDayConvention,
                    calendar, true);

            int i = 0;
            while (i < feeLeg.getNbDates()) {
                LocalDate accrualStartDate = feeLeg.getAccStartDates()[i];
                LocalDate accrualEndDate   = feeLeg.getAccEndDates()[i];

                if ((accrualStartDate.isBefore(tradeDate) || accrualStartDate.isEqual(tradeDate))
                        && (tradeDate.isBefore(accrualEndDate))) {
                    //TODO - Not sure about this logic
                    //Confirm with Jasjit
                    accrualDays.set(accrualStartDate.periodUntil(edd.plusDays(1), ChronoUnit.DAYS));
                    DoubleHolder accrual = new DoubleHolder();
                    if (TDateFunctions.cdsDayCountFraction(
                            accrualStartDate,
                            edd.plusDays(1),
                            paymentDcc,
                            accrual
                    ).equals(ReturnStatus.FAILURE)) {
                        throw new CdsLibraryException("Defaulted.cdsDefaultAccrual()::Error in TDateFunctions.cdsDayCountFraction()");
                    }

                    defaultAccrual.set(defaultAccrual.get() * (accrual.get() * couponRate * notional));
                }
                ++i;
            }
        } catch (Exception ex) {
            logger.error(ex);
            return ReturnStatus.FAILURE;
        }

        return ReturnStatus.SUCCESS;
    }
}
