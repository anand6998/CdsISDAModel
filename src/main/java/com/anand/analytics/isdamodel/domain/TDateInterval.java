package com.anand.analytics.isdamodel.domain;

import com.anand.analytics.isdamodel.utils.DoubleHolder;
import com.anand.analytics.isdamodel.utils.PeriodType;
import com.anand.analytics.isdamodel.utils.ReturnStatus;
import org.apache.log4j.Logger;
import static com.anand.analytics.isdamodel.utils.CdsDateConstants.*;

/**
 * Created by Anand on 10/21/2014.
 */
public class TDateInterval {
    private final static Logger logger = Logger.getLogger(TDateInterval.class);
    public long prd;
    public PeriodType periodType;
    public int flag;

    @Override
    public String toString() {
        return "TDateInterval{" +
                "periodType=" + periodType +
                ", prd=" + prd +
                '}';
    }

    public TDateInterval(long prd, PeriodType periodType, int flag) {
        this.prd = prd;
        this.periodType = periodType;
        this.flag = flag;
    }

    public TDateInterval() {
    }

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

    public ReturnStatus getIntervalInYears(DoubleHolder retValue) {
        double years;
        switch (this.periodType) {
            case A:
            case Y:
                years = prd;
                break;
            case S:
                years = prd / 2.;
                break;
            case Q:
            case I:
            case K:
            case L:
                years = (double) prd / 4.;
                break;
            case M:
            case E:
            case F:
            case G:
            case H:
            case J:
            case T:
                years = (double) prd / MONTHS_PER_YEAR;
                break;
            case W:
                years = (double) prd * DAYS_PER_WEEK / DAYS_PER_YEAR;
                break;
            case D:
                years = (double) prd / DAYS_PER_YEAR;
                break;
            case U:
                years = (double) prd * DAYS_PER_LUNAR_MONTH / DAYS_PER_YEAR;
                break;
            default:
                logger.error("Unknown interval type");
                return ReturnStatus.FAILURE;

        }

        retValue.set(years);
        return ReturnStatus.SUCCESS;
    }
}
