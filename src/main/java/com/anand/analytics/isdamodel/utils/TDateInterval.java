package com.anand.analytics.isdamodel.utils;

/**
 * Created by Anand on 10/21/2014.
 */
public class TDateInterval {
    public int prd;
    public PeriodType periodType;
    public int flag;

    @Override
    public String toString() {
        return "TDateInterval{" +
                "periodType=" + periodType +
                ", prd=" + prd +
                '}';
    }

    public TDateInterval(int prd, PeriodType periodType, int flag) {
        this.prd = prd;
        this.periodType = periodType;
        this.flag = flag;
    }

    public TDateInterval() {
    }

    private final static int MONTHS_PER_YEAR = 12;
    private final static int MONTHS_PER_SEMI = 6;
    private final static int MONTHS_PER_QUARTER = 3;
    private final static int DAYS_PER_WEEK = 7;
    private final static int DAYS_PER_LUNAR_MONTH = 28;

    public static TDateInterval get(int numPeriods, char periodType) {
        TDateInterval dateInterval = new TDateInterval();
        dateInterval.flag = 0;

        //convert to upper case
        periodType = Character.toUpperCase(periodType);

        switch (periodType) {
            case 'A':
            case 'Y':
                dateInterval.periodType = PeriodType.M;
                dateInterval.prd = numPeriods * MONTHS_PER_YEAR;
                break;
            case 'S':
                dateInterval.periodType = PeriodType.M;
                dateInterval.prd = numPeriods * MONTHS_PER_SEMI;
                break;
            case 'Q':
                dateInterval.periodType = PeriodType.M;
                dateInterval.prd = numPeriods * MONTHS_PER_QUARTER;
                break;
            case 'W':
                dateInterval.periodType = PeriodType.D;
                dateInterval.prd = numPeriods * DAYS_PER_WEEK;
                break;
            case 'U': /*Lunar period - 28 days */
                dateInterval.periodType = PeriodType.U;
                dateInterval.prd = numPeriods * DAYS_PER_LUNAR_MONTH;
                break;
            case 'D':                       /* Day                                 */
            case 'M':                       /* Normal Month                        */
            case 'F':                       /* Flexible End of month               */
            case 'E':                       /* End of month unconditional          */
            case 'B':                       /* Beginning of month unconditional    */
            case 'G':                       /* 29th of month                       */
            case 'H':                       /* 30th of month                       */
            case 'I':                       /* IMM date                            */
            case 'J':                       /* monthly IMM date                    */
            case 'K':                       /* Aussie quarterly IMM date           */
            case 'L':                       /* Kiwi quarterly IMM date             */
            case 'T':                       /* equity derivative expiry 3rd Friday */
            case 'V':                       /* virtual IMM dates (fortnightly)     */
                dateInterval.periodType = PeriodType.getPeriodType(periodType);
                dateInterval.prd = numPeriods;
                break;
            default:
                throw new RuntimeException("Invalid period type");
        }

        return dateInterval;
    }
}
