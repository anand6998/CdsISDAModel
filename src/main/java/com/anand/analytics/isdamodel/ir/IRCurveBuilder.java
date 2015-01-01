package com.anand.analytics.isdamodel.ir;

import com.anand.analytics.isdamodel.date.HolidayCalendar;
import com.anand.analytics.isdamodel.domain.*;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.utils.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.List;

import static com.anand.analytics.isdamodel.domain.TDateFunctions.IS_BETWEEN;
import static com.anand.analytics.isdamodel.domain.TDateFunctions.dateFromDateAndOffset;

/**
 * Created by anand on 12/27/14.
 */
public class IRCurveBuilder {
    private final static Logger logger = Logger.getLogger(IRCurveBuilder.class);

    public static TCurve buildIRZeroCurve(
            LocalDate valueDate,
            char[] instrumentNames,
            LocalDate[] dates,
            double[] rates,
            DayCount mmDCC,
            long fixedSwapFreq,
            long floatSwapFreq,
            DayCount fixedSwapDayCount,
            DayCount floatSwapDayCount,
            TBadDayConvention badDayConvention,
            HolidayCalendar calendar
    ) throws CdsLibraryException {
        final List<LocalDate> cashDates = new ArrayList<LocalDate>();
        final List<LocalDate> swapDates = new ArrayList<LocalDate>();

        final List<Double> cashRates = new ArrayList<Double>();
        final List<Double> swapRates = new ArrayList<>();

        final List<String> validInstruments = TInstrumentType.getValuesAsList();

        for (int i = 0; i < instrumentNames.length; i++) {
            String instr = String.valueOf(instrumentNames[i]).toUpperCase();
            if (!validInstruments.contains(instr))
                throw new CdsLibraryException("Invalid InstrumentType");
            TInstrumentType instrumentType = TInstrumentType.getInstrumentType(instr);
            switch(instrumentType) {
                case M:
                    cashDates.add(dates[i]);
                    cashRates.add(rates[i]);
                    break;
                case S:
                    swapDates.add(dates[i]);
                    swapRates.add(rates[i]);
                    break;
                default:
                    throw new CdsLibraryException("Unknown instrument type");
            }
        }

        final LocalDate[] cashDatesArr = cashDates.toArray(new LocalDate[0]);
        final LocalDate[] swapDatesArr = swapDates.toArray(new LocalDate[0]);

        final double[] cashRatesArr = ArrayUtils.toPrimitive( cashRates.toArray(new Double[0]));
        final double[] swapRatesArr = ArrayUtils.toPrimitive( swapRates.toArray(new Double[0]));

        /* Cash instruments */
        ZeroCurve zCurveCash = new ZeroCurve(valueDate, DayCountBasis.ZC_DEFAULT_BASIS, DayCount.ACT_365F);
        TCurve discountZC = null;


        zCurveCash.addRates(cashDatesArr, cashRatesArr, mmDCC);
        buildZeroCurveSwap(zCurveCash,
                discountZC,
                swapDatesArr,
                swapRatesArr,
                swapDatesArr.length,
                fixedSwapFreq,
                floatSwapFreq,
                fixedSwapDayCount,
                floatSwapDayCount,
                3,
                badDayConvention,
                calendar);
        return zCurveCash.toTCurve();
    }

    private static TCurve buildZeroCurveSwap(ZeroCurve zeroCurve,
                                           TCurve discountZC,
                                           LocalDate swapDates[],
                                           double swapRates[],
                                           int numSwaps,
                                           long fixedSwapFreq,
                                           long floatSwapFreq,
                                           DayCount fixedSwapDayCount,
                                           DayCount floatSwapDayCount,
                                           int fwdLength,
                                           TBadDayConvention badDayConvention,
                                           HolidayCalendar calendar) throws CdsLibraryException {

        TStubPos stubPos = TStubPos.DEFAULT_AUTO;
        TCurve tcSwaps = null;
        if (numSwaps == 0) {
            tcSwaps = zeroCurve.toTCurve().clone();
            return tcSwaps;
        }

        checkSwapInputs(zeroCurve.toTCurve(),
                discountZC,
                swapDates,
                swapRates,
                numSwaps,
                fixedSwapFreq,
                floatSwapFreq,
                fixedSwapDayCount,
                floatSwapDayCount,
                fwdLength,
                badDayConvention,
                calendar);

        LocalDate lastStubDate;
        if (zeroCurve.getfNumItems() < 1) {
            lastStubDate = zeroCurve.getValueDate();
        } else {
            lastStubDate = zeroCurve.getDates()[zeroCurve.getfNumItems() - 1];
        }

        int offset = 0;
        List<LocalDate> offsetSwapDatesList = new ArrayList<>();
        List<Double> offsetSwapRatesList = new ArrayList<>();

        while(numSwaps > 0 && swapDates[offset].isBefore(lastStubDate)) {
            offset++;
            numSwaps--;

        }


        //TODO - replace this with System.arraycopy
        //region
        for (int i = offset; i < swapDates.length; i++) {
            offsetSwapDatesList.add(swapDates[i]);
            offsetSwapRatesList.add(swapRates[i]);
        }

        LocalDate[] offsetSwapDates = offsetSwapDatesList.toArray(new LocalDate[0]);
        double[] offsetSwapRates = ArrayUtils.toPrimitive( offsetSwapRatesList.toArray(new Double[0]));
        //endregion

        TInterpData tInterpData = null;
        TBadDayList tBadDayList = null;

        if (numSwaps > 0) {
            zcAddSwaps(
                    zeroCurve,
                    discountZC,
                    offsetSwapDates,
                    offsetSwapRates,
                    numSwaps,
                    fixedSwapFreq,
                    floatSwapFreq,
                    fixedSwapDayCount,
                    floatSwapDayCount,
                    TInterpType.FLAT_FORWARDS,
                    tInterpData,
                    tBadDayList,
                    badDayConvention,
                    stubPos,
                    calendar


            );
        }
        return null;
    }

    private static void zcAddSwaps(ZeroCurve zeroCurve,
                                   TCurve discountZC,
                                   LocalDate[] inDates,
                                   double[] inRates,
                                   int numSwaps,
                                   long fixedSwapFreq,
                                   long floatSwapFreq,
                                   DayCount fixedSwapDayCount,
                                   DayCount floatSwapDayCount,
                                   TInterpType flatForwards,
                                   TInterpData tInterpData,
                                   TBadDayList tBadDayList,
                                   TBadDayConvention badDayConvention,
                                   TStubPos stubPos,
                                   HolidayCalendar calendar) throws CdsLibraryException {
        try {
            Validate.notNull(zeroCurve, "zeroCurve == null");
            Validate.isTrue(zeroCurve.getfNumItems() > 0, "zeroCurve.getfNumItems < 1");

            boolean cond = !(tBadDayList != null && !(badDayConvention.equals(TBadDayConvention.NONE)));
            Validate.isTrue(cond, "Invalid badDayConv / badDayList");

            TSwapDate[] swapDates = null;
            if (tBadDayList != null) {
                //this should never be true for us
                //TODO - may code later
                //use the holiday list to define the adjustments
            } else {
                swapDates = swapDatesNewFromOriginal(
                        zeroCurve.getValueDate(),
                        fixedSwapFreq,
                        inDates,
                        numSwaps,
                        tBadDayList,
                        badDayConvention,
                        calendar
                );

            }
            Validate.notNull(swapDates, "swapDates == null");

        } catch (Exception ex) {
            logger.error(ex);
            throw new CdsLibraryException(ex.getMessage());
        }
    }

    private static TSwapDate[] swapDatesNewFromOriginal(LocalDate valueDate,
                                                       long freq,
                                                       LocalDate[] swapDates,
                                                       int numDates,
                                                       TBadDayList tBadDayList,
                                                       TBadDayConvention badDayConvention,
                                                       HolidayCalendar calendar) throws CdsLibraryException {
        TSwapDate[] tSwapDates = new TSwapDate[numDates];
        for (int idx = 0; idx < numDates; idx++) {
            TSwapDate tSwapDate = new TSwapDate();
            tSwapDate.setOriginalDate(swapDates[idx]);

            LocalDate adjustedDate = calendar.getNextBusinessDay(swapDates[idx], badDayConvention);
            tSwapDate.setAdjustedDate(adjustedDate);

            setPreviousDateAndOnCycle(valueDate, swapDates[idx], freq, tSwapDate);
            tSwapDates[idx] = (tSwapDate);
        }

        return tSwapDates;

    }

    private static void setPreviousDateAndOnCycle(LocalDate valueDate, LocalDate origDate, long freq, TSwapDate tSwapDate)
    throws CdsLibraryException {
        try {
            TDateInterval interval = CdsFunctions.freq2TDateInterval(freq);
            IntHolder numItervals = new IntHolder();
            if (valueDate.getDayOfMonth() <= 28 &&
                    origDate.getDayOfMonth() <= 28) {
                /**
                 * We assume we can only be on cycle if date is not on or after the 29th of the month
                 */
                /**
                 * Find out if adjusted date is on cycle
                 */

                IntHolder extraDays = new IntHolder();
                ReturnStatus status = countDates(valueDate,
                        origDate, interval, numItervals, extraDays);

                tSwapDate.setOnCycle((extraDays.get() == 0));
            } else {
                tSwapDate.setOnCycle(false);
            }

            /**
             * Now compute the prev date. If on cycle - count forward from value date
             * If off cycle count backward from maturity date
             */
            if (tSwapDate.isOnCycle()) {
                LocalDate prevDate = dateFromDateAndOffset(valueDate, interval, numItervals.get() - 1);
                tSwapDate.setPreviousDate(prevDate);
            } else {
                LocalDate prevDate = dateFromDateAndOffset(origDate, interval, -1);
                tSwapDate.setPreviousDate(prevDate);
            }
        } catch(Exception ex) {
            logger.error(ex);
            throw new CdsLibraryException(ex.getMessage());
        }
    }

    private static ReturnStatus countDates(LocalDate fromDate, LocalDate toDate, TDateInterval interval, IntHolder numItervals, IntHolder extraDays) {
        try {
            boolean isValid = checkDateInterval(interval, fromDate, toDate);
            if (!isValid)
                return ReturnStatus.FAILURE;

            DoubleHolder intervalYears = new DoubleHolder();
            if (interval.getIntervalInYears(intervalYears).equals(ReturnStatus.FAILURE)) {
                logger.error("Error converting interval to years");
                return ReturnStatus.FAILURE;
            }

            double fromToYears = (double) (fromDate.periodUntil(toDate, ChronoUnit.DAYS)) / CdsDateConstants.DAYS_PER_YEAR;
            int lowNumIntervals = Math.max(0, (int) Math.floor(Math.abs(fromToYears / intervalYears.get())) - 2);
            int index = lowNumIntervals;
            LocalDate currDate = dateFromDateAndOffset(fromDate, interval, index);
            LocalDate lastDate = currDate;

            while (IS_BETWEEN(currDate, fromDate, toDate)) {
                ++index;
                lastDate = currDate;
                currDate = dateFromDateAndOffset(fromDate, interval, index);
            }

            numItervals.set(index - 1);
            if (numItervals.get() < lowNumIntervals) {
                logger.error("Failed in countDates (" + fromDate + ")-( " + toDate + ")");
                return ReturnStatus.FAILURE;
            }

            extraDays.set((int) Math.abs(lastDate.periodUntil(toDate, ChronoUnit.DAYS)));
        } catch (Exception ex) {
            logger.error(ex);
            return ReturnStatus.FAILURE;
        }

        return ReturnStatus.SUCCESS;
    }

    private static boolean checkDateInterval(TDateInterval interval, LocalDate fromDate, LocalDate toDate) {
        if (interval.prd == 0)
            return false;
        if (fromDate.periodUntil(toDate, ChronoUnit.DAYS) < 0) {
            logger.error("Invalid to and from dates");
            return false;
        }

        return true;
    }

    private static void checkSwapInputs(TCurve zeroCurve,
                                        TCurve discountZC,
                                        LocalDate[] swapDates,
                                        double[] swapRates,
                                        int numSwaps,
                                        long fixedSwapFreq,
                                        long floatSwapFreq,
                                        DayCount fixedSwapDayCount,
                                        DayCount floatSwapDayCount,
                                        int fwdLength,
                                        TBadDayConvention badDayConvention,
                                        HolidayCalendar calendar) throws CdsLibraryException {


    }

}
