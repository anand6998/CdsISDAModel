package com.anand.analytics.isdamodel.cds;

/**
 * Created by Anand on 12/24/2014.
 */


import com.anand.analytics.isdamodel.domain.TBadDayConvention;
import com.anand.analytics.isdamodel.domain.TContingentLeg;
import com.anand.analytics.isdamodel.domain.TCurve;
import com.anand.analytics.isdamodel.domain.TDateInterval;
import com.anand.analytics.isdamodel.domain.TFeeLeg;
import com.anand.analytics.isdamodel.domain.TProtPayConv;
import com.anand.analytics.isdamodel.domain.TStubMethod;
import com.anand.analytics.isdamodel.exception.CdsLibraryException;
import com.anand.analytics.isdamodel.utils.CdsUtils;
import com.anand.analytics.isdamodel.utils.DayCount;
import com.anand.analytics.isdamodel.utils.DayCountBasis;
import com.anand.analytics.isdamodel.utils.DoubleHolder;
import com.anand.analytics.isdamodel.utils.ExcelFunctions;
import com.anand.analytics.isdamodel.utils.IntHolder;
import com.anand.analytics.isdamodel.utils.PeriodType;
import com.anand.analytics.isdamodel.utils.ReturnStatus;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.threeten.bp.LocalDate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

import static com.anand.analytics.isdamodel.domain.TDateFunctions.cdsDayCountFraction;

/**
 * Created by Anand on 10/28/2014.
 */
public class TestCdsFunctions {

    final static Logger logger = Logger.getLogger(TestCdsFunctions.class);

    @Test
    public void testDcfActAct() {
        LocalDate d1 = LocalDate.of(2007, 12, 27);
        LocalDate d2 = LocalDate.of(2008, 2, 28);

        DoubleHolder result = new DoubleHolder();
        cdsDayCountFraction(d1, d2, DayCount.ACT_ACT, result);
        System.out.println(result.get());
    }

    private TCurve setupExampleCurve() throws Exception {
        LocalDate[] dates = new LocalDate[64];
        double[] rates = new double[64];

        LocalDate baseDate = LocalDate.of(2008, 9, 22);
        DayCount dayCount = DayCount.ACT_360;
        DayCountBasis basis = DayCountBasis.ANNUAL_BASIS;

        //region dates
        {
            dates[0] = LocalDate.of(2008, 10, 22);
            dates[1] = LocalDate.of(2008, 11, 24);
            dates[2] = LocalDate.of(2008, 12, 22);
            dates[3] = LocalDate.of(2009, 3, 23);
            dates[4] = LocalDate.of(2009, 6, 22);
            dates[5] = LocalDate.of(2009, 9, 22);
            dates[6] = LocalDate.of(2010, 3, 22);
            dates[7] = LocalDate.of(2010, 9, 22);
            dates[8] = LocalDate.of(2011, 3, 22);
            dates[9] = LocalDate.of(2011, 9, 22);
            dates[10] = LocalDate.of(2012, 3, 22);
            dates[11] = LocalDate.of(2012, 9, 24);
            dates[12] = LocalDate.of(2013, 3, 22);
            dates[13] = LocalDate.of(2013, 9, 23);
            dates[14] = LocalDate.of(2014, 3, 24);
            dates[15] = LocalDate.of(2014, 9, 22);
            dates[16] = LocalDate.of(2015, 3, 23);
            dates[17] = LocalDate.of(2015, 9, 22);
            dates[18] = LocalDate.of(2016, 3, 22);
            dates[19] = LocalDate.of(2016, 9, 22);
            dates[20] = LocalDate.of(2017, 3, 22);
            dates[21] = LocalDate.of(2017, 9, 22);
            dates[22] = LocalDate.of(2018, 3, 22);
            dates[23] = LocalDate.of(2018, 9, 24);
            dates[24] = LocalDate.of(2019, 3, 22);
            dates[25] = LocalDate.of(2019, 9, 23);
            dates[26] = LocalDate.of(2020, 3, 23);
            dates[27] = LocalDate.of(2020, 9, 22);
            dates[28] = LocalDate.of(2021, 3, 22);
            dates[29] = LocalDate.of(2021, 9, 22);
            dates[30] = LocalDate.of(2022, 3, 22);
            dates[31] = LocalDate.of(2022, 9, 22);
            dates[32] = LocalDate.of(2023, 3, 22);
            dates[33] = LocalDate.of(2023, 9, 22);
            dates[34] = LocalDate.of(2024, 3, 22);
            dates[35] = LocalDate.of(2024, 9, 23);
            dates[36] = LocalDate.of(2025, 3, 24);
            dates[37] = LocalDate.of(2025, 9, 22);
            dates[38] = LocalDate.of(2026, 3, 23);
            dates[39] = LocalDate.of(2026, 9, 22);
            dates[40] = LocalDate.of(2027, 3, 22);
            dates[41] = LocalDate.of(2027, 9, 22);
            dates[42] = LocalDate.of(2028, 3, 22);
            dates[43] = LocalDate.of(2028, 9, 22);
            dates[44] = LocalDate.of(2029, 3, 22);
            dates[45] = LocalDate.of(2029, 9, 24);
            dates[46] = LocalDate.of(2030, 3, 22);
            dates[47] = LocalDate.of(2030, 9, 23);
            dates[48] = LocalDate.of(2031, 3, 24);
            dates[49] = LocalDate.of(2031, 9, 22);
            dates[50] = LocalDate.of(2032, 3, 22);
            dates[51] = LocalDate.of(2032, 9, 22);
            dates[52] = LocalDate.of(2033, 3, 22);
            dates[53] = LocalDate.of(2033, 9, 22);
            dates[54] = LocalDate.of(2034, 3, 22);
            dates[55] = LocalDate.of(2034, 9, 22);
            dates[56] = LocalDate.of(2035, 3, 22);
            dates[57] = LocalDate.of(2035, 9, 24);
            dates[58] = LocalDate.of(2036, 3, 24);
            dates[59] = LocalDate.of(2036, 9, 22);
            dates[60] = LocalDate.of(2037, 3, 23);
            dates[61] = LocalDate.of(2037, 9, 22);
            dates[62] = LocalDate.of(2038, 3, 22);
            dates[63] = LocalDate.of(2038, 9, 22);

        }
        //endregion

        //region rates
        {
            rates[0] = 0.00452115893602745;
            rates[1] = 0.00965814197655757;
            rates[2] = 0.0125671956942268;
            rates[3] = 0.0180899961797023;
            rates[4] = 0.0196671010062783;
            rates[5] = 0.0211274166666666;
            rates[6] = 0.0180953476043511;
            rates[7] = 0.01655763824251;
            rates[8] = 0.0188060976441178;
            rates[9] = 0.0203327420803128;
            rates[10] = 0.0220108247958211;
            rates[11] = 0.0232962726914658;
            rates[12] = 0.0245799199096257;
            rates[13] = 0.0256434938060697;
            rates[14] = 0.0266419886967881;
            rates[15] = 0.0274753426521101;
            rates[16] = 0.0282242175211318;
            rates[17] = 0.0288701171820721;
            rates[18] = 0.0294793831512583;
            rates[19] = 0.0300184917099712;
            rates[20] = 0.0305172304772172;
            rates[21] = 0.0309681437245734;
            rates[22] = 0.0314037831595382;
            rates[23] = 0.0318066571736947;
            rates[24] = 0.0322047004081603;
            rates[25] = 0.0325789574898254;
            rates[26] = 0.0330057686820455;
            rates[27] = 0.0333993426974299;
            rates[28] = 0.0337143923591572;
            rates[29] = 0.0340101304958846;
            rates[30] = 0.0342795776461313;
            rates[31] = 0.0345340014538034;
            rates[32] = 0.0347670764614674;
            rates[33] = 0.0349882759154866;
            rates[34] = 0.0350460265368673;
            rates[35] = 0.0351010462311578;
            rates[36] = 0.0351518803475177;
            rates[37] = 0.0351997366165311;
            rates[38] = 0.0352448692543093;
            rates[39] = 0.0352877320837328;
            rates[40] = 0.0353278436101232;
            rates[41] = 0.0353664765505937;
            rates[42] = 0.0354027268337034;
            rates[43] = 0.0354375404716664;
            rates[44] = 0.035399368373132;
            rates[45] = 0.0353620196126478;
            rates[46] = 0.0353277486657106;
            rates[47] = 0.0352939344601833;
            rates[48] = 0.0352621551892058;
            rates[49] = 0.035231753932973;
            rates[50] = 0.0352026429631944;
            rates[51] = 0.0351744416776323;
            rates[52] = 0.0351478326359755;
            rates[53] = 0.0351218645120064;
            rates[54] = 0.0351094587893486;
            rates[55] = 0.0350973323358299;
            rates[56] = 0.0350858536589047;
            rates[57] = 0.0350744969345695;
            rates[58] = 0.0350637916627374;
            rates[59] = 0.0350534675184635;
            rates[60] = 0.0350435045044457;
            rates[61] = 0.0350338320519035;
            rates[62] = 0.0350245886364577;
            rates[63] = 0.0350155051162542;

        }
        //endregion
        return new TCurve(baseDate, dates, rates, basis, dayCount);

    }

    @Test
    public void testCdsUpfrontChargeBestBuy20191220() throws Exception {
        LocalDate today = LocalDate.of(2014, 10, 2);
        LocalDate valueDate = LocalDate.of(2014, 10, 7);
        LocalDate benchmarkStartDate = LocalDate.of(2014, 9, 20);
        LocalDate stepinDate = LocalDate.of(2014, 10, 3);
        LocalDate startDate = benchmarkStartDate;
        LocalDate endDate = LocalDate.of(2019, 12, 20);
        double couponRate = 0.05;
        boolean payAccruedOnDefault = true;
        TDateInterval tDateInterval = new TDateInterval(3, PeriodType.M, 0);
        TStubMethod stubType = new TStubMethod(false, false);
        DayCount accruedDCC = DayCount.ACT_360;
        TBadDayConvention badDayConvention = TBadDayConvention.FOLLOW;
        String calendar = "None";
        TCurve discountCurve = setUpTCurve();
        double oneSpread = 0.025356603757534135;

        double recoveryRate = 0.4;
        boolean payAccruedAtStart = true;

        DoubleHolder upfrontCharge = new DoubleHolder();
        CdsOne.cdsCdsoneUpfrontCharge(today,
                valueDate,
                benchmarkStartDate,
                stepinDate,
                startDate,
                endDate,
                couponRate,
                payAccruedOnDefault,
                tDateInterval,
                stubType,
                accruedDCC,
                badDayConvention,
                calendar,
                discountCurve,
                oneSpread,
                recoveryRate,
                payAccruedAtStart,
                upfrontCharge);

        double DELTA = 1e-10;
        double expectedValue = -0.112650073159086;

        System.out.println(upfrontCharge.get());
        Assert.assertEquals(expectedValue, upfrontCharge.get(), DELTA);

    }


    /**
     * Converts spread to upfront
     *
     * @throws Exception
     */
    @Test
    public void testCdsOneUpfrontCharge() throws Exception {
        LocalDate today = LocalDate.of(2014, 10, 2);
        LocalDate valueDate = LocalDate.of(2014, 10, 7);
        LocalDate benchmarkStartDate = LocalDate.of(2014, 9, 20);
        LocalDate stepinDate = LocalDate.of(2014, 10, 3);
        LocalDate startDate = benchmarkStartDate;
        LocalDate endDate = LocalDate.of(2018, 12, 20);
        double couponRate = 0.01;
        boolean payAccruedOnDefault = true;
        TDateInterval tDateInterval = new TDateInterval(3, PeriodType.M, 0);
        TStubMethod stubType = new TStubMethod(false, false);
        DayCount accruedDCC = DayCount.ACT_360;
        TBadDayConvention badDayConvention = TBadDayConvention.FOLLOW;
        String calendar = "None";
        TCurve discountCurve = setUpTCurve();
        double oneSpread = 0.011313613279366955;

        double recoveryRate = 0.4;
        boolean payAccruedAtStart = true;

        DoubleHolder upfrontCharge = new DoubleHolder();
        CdsOne.cdsCdsoneUpfrontCharge(today,
                valueDate,
                benchmarkStartDate,
                stepinDate,
                startDate,
                endDate,
                couponRate,
                payAccruedOnDefault,
                tDateInterval,
                stubType,
                accruedDCC,
                badDayConvention,
                calendar,
                discountCurve,
                oneSpread,
                recoveryRate,
                payAccruedAtStart,
                upfrontCharge);

        double DELTA = 1e-10;
        double expectedValue = 0.00526027521176873;
        System.out.println(upfrontCharge.get());

        Assert.assertEquals(expectedValue, upfrontCharge.get(), DELTA);
    }

    /**
     * Converts upfront to spread
     *
     * @throws Exception
     */
    @Test
    public void testCdsOneSpread() throws Exception {
        LocalDate today = LocalDate.of(2014, 10, 2);
        LocalDate valueDate = LocalDate.of(2014, 10, 7);
        LocalDate benchmarkStartDate = LocalDate.of(2014, 9, 20);
        LocalDate stepinDate = LocalDate.of(2014, 10, 3);
        LocalDate startDate = benchmarkStartDate;
        LocalDate endDate = LocalDate.of(2018, 12, 20);
        double couponRate = 0.01;
        boolean payAccruedOnDefault = true;
        TDateInterval tDateInterval = new TDateInterval(3, PeriodType.M, 0);
        TStubMethod stubType = new TStubMethod(false, false);
        DayCount accruedDCC = DayCount.ACT_360;
        TBadDayConvention badDayConvention = TBadDayConvention.FOLLOW;
        String calendar = "None";
        TCurve discountCurve = setUpTCurve();
        double upfrontCharge = 0.00565876;
        double recoveryRate = 0.4;
        boolean payAccruedAtStart = true;

        DoubleHolder oneSpread = new DoubleHolder();

        CdsOne.cdsCdsoneSpread(today,
                valueDate,
                benchmarkStartDate,
                stepinDate,
                startDate,
                endDate,
                couponRate,
                payAccruedOnDefault,
                tDateInterval,
                stubType,
                accruedDCC,
                badDayConvention,
                calendar,
                discountCurve,
                upfrontCharge,
                recoveryRate,
                payAccruedAtStart,
                oneSpread);

        double DELTA = 1e-10;
        double expectedValue = 0.011413613279366954;
        System.out.println("Upfront to spread : " + oneSpread.get());
        Assert.assertEquals(expectedValue, oneSpread.get(), DELTA);
    }

    @Test
    public void testTCurveSerialization() throws Exception {
        TCurve tCurve = setUpTCurve();

        TCurveStorable storable = new TCurveStorable(tCurve.getBaseDate(),
                tCurve.getDates(),
                tCurve.getRates(),
                tCurve.getBasis(),
                tCurve.getDayCountConv());


        StringWriter writer = new StringWriter();
        JAXBContext context = JAXBContext.newInstance(TCurveStorable.class);
        Marshaller m = context.createMarshaller();

        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(storable, writer);

        System.out.println(writer.toString());
    }

    @Test
    public void testFeeLeg() throws Exception {
        LocalDate startDate = LocalDate.of(2014, 9, 20);
        LocalDate endDate = LocalDate.of(2018, 12, 20);
        boolean payAccruedOnDefault = true;
        TDateInterval tDateInterval = new TDateInterval(3, PeriodType.M, 0);
        TStubMethod tStubMethod = new TStubMethod(false, false);
        double notional = 1.0;
        double coupon = 0.01;

        DayCount paymentDcc = DayCount.ACT_360;
        String calendar = "None";
        boolean protectStart = true;
        TBadDayConvention badDayConvention = TBadDayConvention.FOLLOW;

        TFeeLeg feeLeg = new TFeeLeg(startDate, endDate, payAccruedOnDefault,
                tDateInterval, tStubMethod,
                notional,
                coupon,
                paymentDcc,
                badDayConvention,
                calendar,
                protectStart);

        LocalDate today = LocalDate.of(2014, 10, 2);
        LocalDate stepinDate = LocalDate.of(2014, 10, 3);
        LocalDate valueDate = LocalDate.of(2014, 10, 7);

        LocalDate[] dates = new LocalDate[]{endDate};

        //+		fArray	0x0d8c9e40 {fDate=152659 fRate=0.016666666666666666 }	TRatePt *
        double[] rates = new double[]{0.016666666666666666};
        TCurve spreadCurve = new TCurve(today, dates, rates, DayCountBasis.CONTINUOUS_BASIS, DayCount.ACT_365F);
        TCurve discountCurve = setUpTCurve();
        DoubleHolder result = new DoubleHolder();
        feeLeg.getPV(today, valueDate, stepinDate, discountCurve, spreadCurve, true, result);
        System.out.println(result.get());

        double DELTA = 1e-10;
        double expectedValue = 0.040243012207293836;
        Assert.assertEquals(expectedValue, result.get(), DELTA);
    }

    @Test
    public void testContingentLegPV() throws Exception {
        LocalDate startDate = LocalDate.of(2014, 10, 3);
        LocalDate endDate = LocalDate.of(2018, 12, 20);
        double notional = 1.0;
        boolean protectStart = true;


        TContingentLeg contingentLeg = new TContingentLeg(startDate, endDate, notional, protectStart, TProtPayConv.PROT_PAY_DEF);
        LocalDate today = LocalDate.of(2014, 10, 2);
        LocalDate valueDate = LocalDate.of(2014, 10, 7);
        LocalDate stepinDate = LocalDate.of(2014, 10, 3);

        LocalDate baseDate = today;
        LocalDate[] dates = new LocalDate[]{endDate};

        //fRate	0.017004304359210076	double
        double[] rates = new double[]{0.017004304359210076};

        TCurve spreadCurve = new TCurve(baseDate, dates, rates, DayCountBasis.ANNUAL_BASIS, DayCount.ACT_365F);
        TCurve discountCurve = setUpTCurve();
        double recoveryRate = 0.4;

        DoubleHolder pv = new DoubleHolder();
        contingentLeg.getPV(today, valueDate, stepinDate, discountCurve, spreadCurve, recoveryRate, pv);
        System.out.println(pv.get());

        double expectedValue = 0.040226910631177989;
        double DELTA = 1e-10;
        Assert.assertEquals(expectedValue, pv.get(), DELTA);
    }


    @Test
    public void testBinarySearchLong() throws Exception {
        TCurve tCurve = setUpTCurve();
        LocalDate[] xArray = tCurve.getDates();

        LocalDate xDesired = LocalDate.of(2019, 6, 20);
        IntHolder exact = new IntHolder(0);
        IntHolder loBound = new IntHolder(0);
        IntHolder hiBound = new IntHolder(0);

        ReturnStatus status = CdsUtils.binarySearchLong(xDesired, xArray, exact, loBound, hiBound);
        System.out.println(exact.get());
        System.out.println(loBound.get());
        System.out.println(hiBound.get());

        Assert.assertEquals(-1, exact.get());
        Assert.assertEquals(29, loBound.get());
        Assert.assertEquals(30, hiBound.get());
    }

    @Test
    public void testDateIntervalParser() throws Exception {
        String dateIntervalPeriod = "-3M";
        TDateInterval dateInterval = ExcelFunctions.cdsStringToDateInterval(dateIntervalPeriod);
        System.out.println(dateInterval);
    }

    @Test
    public void testStubMethodParser() throws CdsLibraryException {
        String stubMethodString = "F/S";
        TStubMethod stubMethod = ExcelFunctions.cdsStringToStubMethod(stubMethodString);
        System.out.println(stubMethod);
    }

    @Test
    public void testDccParser() throws CdsLibraryException {
        String dccString = "ACT/365F";
        DayCount dayCount = ExcelFunctions.cdsStringToDayCountConv(dccString);
        System.out.println(dayCount);
    }

    public static TCurve setUpTCurve() throws Exception {

        LocalDate baseDate = LocalDate.of(2014, 10, 7);
        LocalDate[] dates = new LocalDate[212];
        double[] rates = new double[212];

        dates[0] = LocalDate.of(2014, 10, 8);
        dates[1] = LocalDate.of(2014, 10, 9);
        dates[2] = LocalDate.of(2014, 10, 14);
        dates[3] = LocalDate.of(2014, 10, 21);
        dates[4] = LocalDate.of(2014, 11, 7);
        dates[5] = LocalDate.of(2014, 12, 8);
        dates[6] = LocalDate.of(2015, 1, 7);
        dates[7] = LocalDate.of(2015, 2, 9);
        dates[8] = LocalDate.of(2015, 3, 9);
        dates[9] = LocalDate.of(2015, 4, 7);
        dates[10] = LocalDate.of(2015, 5, 7);
        dates[11] = LocalDate.of(2015, 6, 8);
        dates[12] = LocalDate.of(2015, 7, 7);
        dates[13] = LocalDate.of(2015, 8, 7);
        dates[14] = LocalDate.of(2015, 9, 8);
        dates[15] = LocalDate.of(2015, 10, 7);
        dates[16] = LocalDate.of(2016, 1, 7);
        dates[17] = LocalDate.of(2016, 4, 7);
        dates[18] = LocalDate.of(2016, 7, 7);
        dates[19] = LocalDate.of(2016, 10, 7);
        dates[20] = LocalDate.of(2017, 1, 9);
        dates[21] = LocalDate.of(2017, 4, 7);
        dates[22] = LocalDate.of(2017, 7, 7);
        dates[23] = LocalDate.of(2017, 10, 10);
        dates[24] = LocalDate.of(2018, 1, 8);
        dates[25] = LocalDate.of(2018, 4, 9);
        dates[26] = LocalDate.of(2018, 7, 9);
        dates[27] = LocalDate.of(2018, 10, 9);
        dates[28] = LocalDate.of(2019, 1, 9);
        dates[29] = LocalDate.of(2019, 4, 9);
        dates[30] = LocalDate.of(2019, 7, 9);
        dates[31] = LocalDate.of(2019, 10, 7);
        dates[32] = LocalDate.of(2020, 1, 7);
        dates[33] = LocalDate.of(2020, 4, 7);
        dates[34] = LocalDate.of(2020, 7, 7);
        dates[35] = LocalDate.of(2020, 10, 7);
        dates[36] = LocalDate.of(2021, 1, 7);
        dates[37] = LocalDate.of(2021, 4, 7);
        dates[38] = LocalDate.of(2021, 7, 7);
        dates[39] = LocalDate.of(2021, 10, 7);
        dates[40] = LocalDate.of(2022, 1, 7);
        dates[41] = LocalDate.of(2022, 4, 7);
        dates[42] = LocalDate.of(2022, 7, 7);
        dates[43] = LocalDate.of(2022, 10, 7);
        dates[44] = LocalDate.of(2023, 1, 7);
        dates[45] = LocalDate.of(2023, 4, 7);
        dates[46] = LocalDate.of(2023, 7, 7);
        dates[47] = LocalDate.of(2023, 10, 10);
        dates[48] = LocalDate.of(2024, 1, 10);
        dates[49] = LocalDate.of(2024, 4, 10);
        dates[50] = LocalDate.of(2024, 7, 10);
        dates[51] = LocalDate.of(2024, 10, 7);
        dates[52] = LocalDate.of(2025, 1, 7);
        dates[53] = LocalDate.of(2025, 4, 7);
        dates[54] = LocalDate.of(2025, 7, 7);
        dates[55] = LocalDate.of(2025, 10, 7);
        dates[56] = LocalDate.of(2026, 1, 7);
        dates[57] = LocalDate.of(2026, 4, 7);
        dates[58] = LocalDate.of(2026, 7, 7);
        dates[59] = LocalDate.of(2026, 10, 7);
        dates[60] = LocalDate.of(2027, 1, 7);
        dates[61] = LocalDate.of(2027, 4, 7);
        dates[62] = LocalDate.of(2027, 7, 7);
        dates[63] = LocalDate.of(2027, 10, 7);
        dates[64] = LocalDate.of(2028, 1, 7);
        dates[65] = LocalDate.of(2028, 4, 7);
        dates[66] = LocalDate.of(2028, 7, 7);
        dates[67] = LocalDate.of(2028, 10, 10);
        dates[68] = LocalDate.of(2029, 1, 10);
        dates[69] = LocalDate.of(2029, 4, 10);
        dates[70] = LocalDate.of(2029, 7, 10);
        dates[71] = LocalDate.of(2029, 10, 9);
        dates[72] = LocalDate.of(2030, 1, 9);
        dates[73] = LocalDate.of(2030, 4, 9);
        dates[74] = LocalDate.of(2030, 7, 9);
        dates[75] = LocalDate.of(2030, 10, 7);
        dates[76] = LocalDate.of(2031, 1, 7);
        dates[77] = LocalDate.of(2031, 4, 7);
        dates[78] = LocalDate.of(2031, 7, 7);
        dates[79] = LocalDate.of(2031, 10, 7);
        dates[80] = LocalDate.of(2032, 1, 7);
        dates[81] = LocalDate.of(2032, 4, 7);
        dates[82] = LocalDate.of(2032, 7, 7);
        dates[83] = LocalDate.of(2032, 10, 7);
        dates[84] = LocalDate.of(2033, 1, 7);
        dates[85] = LocalDate.of(2033, 4, 7);
        dates[86] = LocalDate.of(2033, 7, 7);
        dates[87] = LocalDate.of(2033, 10, 7);
        dates[88] = LocalDate.of(2034, 1, 7);
        dates[89] = LocalDate.of(2034, 4, 7);
        dates[90] = LocalDate.of(2034, 7, 7);
        dates[91] = LocalDate.of(2034, 10, 10);
        dates[92] = LocalDate.of(2035, 1, 10);
        dates[93] = LocalDate.of(2035, 4, 10);
        dates[94] = LocalDate.of(2035, 7, 10);
        dates[95] = LocalDate.of(2035, 10, 9);
        dates[96] = LocalDate.of(2036, 1, 9);
        dates[97] = LocalDate.of(2036, 4, 9);
        dates[98] = LocalDate.of(2036, 7, 9);
        dates[99] = LocalDate.of(2036, 10, 7);
        dates[100] = LocalDate.of(2037, 1, 7);
        dates[101] = LocalDate.of(2037, 4, 7);
        dates[102] = LocalDate.of(2037, 7, 7);
        dates[103] = LocalDate.of(2037, 10, 7);
        dates[104] = LocalDate.of(2038, 1, 7);
        dates[105] = LocalDate.of(2038, 4, 7);
        dates[106] = LocalDate.of(2038, 7, 7);
        dates[107] = LocalDate.of(2038, 10, 7);
        dates[108] = LocalDate.of(2039, 1, 7);
        dates[109] = LocalDate.of(2039, 4, 7);
        dates[110] = LocalDate.of(2039, 7, 7);
        dates[111] = LocalDate.of(2039, 10, 7);
        dates[112] = LocalDate.of(2040, 1, 7);
        dates[113] = LocalDate.of(2040, 4, 7);
        dates[114] = LocalDate.of(2040, 7, 7);
        dates[115] = LocalDate.of(2040, 10, 9);
        dates[116] = LocalDate.of(2041, 1, 9);
        dates[117] = LocalDate.of(2041, 4, 9);
        dates[118] = LocalDate.of(2041, 7, 9);
        dates[119] = LocalDate.of(2041, 10, 7);
        dates[120] = LocalDate.of(2042, 1, 7);
        dates[121] = LocalDate.of(2042, 4, 7);
        dates[122] = LocalDate.of(2042, 7, 7);
        dates[123] = LocalDate.of(2042, 10, 7);
        dates[124] = LocalDate.of(2043, 1, 7);
        dates[125] = LocalDate.of(2043, 4, 7);
        dates[126] = LocalDate.of(2043, 7, 7);
        dates[127] = LocalDate.of(2043, 10, 7);
        dates[128] = LocalDate.of(2044, 1, 7);
        dates[129] = LocalDate.of(2044, 4, 7);
        dates[130] = LocalDate.of(2044, 7, 7);
        dates[131] = LocalDate.of(2044, 10, 7);
        dates[132] = LocalDate.of(2045, 1, 7);
        dates[133] = LocalDate.of(2045, 4, 7);
        dates[134] = LocalDate.of(2045, 7, 7);
        dates[135] = LocalDate.of(2045, 10, 10);
        dates[136] = LocalDate.of(2046, 1, 10);
        dates[137] = LocalDate.of(2046, 4, 10);
        dates[138] = LocalDate.of(2046, 7, 10);
        dates[139] = LocalDate.of(2046, 10, 9);
        dates[140] = LocalDate.of(2047, 1, 9);
        dates[141] = LocalDate.of(2047, 4, 9);
        dates[142] = LocalDate.of(2047, 7, 9);
        dates[143] = LocalDate.of(2047, 10, 7);
        dates[144] = LocalDate.of(2048, 1, 7);
        dates[145] = LocalDate.of(2048, 4, 7);
        dates[146] = LocalDate.of(2048, 7, 7);
        dates[147] = LocalDate.of(2048, 10, 7);
        dates[148] = LocalDate.of(2049, 1, 7);
        dates[149] = LocalDate.of(2049, 4, 7);
        dates[150] = LocalDate.of(2049, 7, 7);
        dates[151] = LocalDate.of(2049, 10, 7);
        dates[152] = LocalDate.of(2050, 1, 7);
        dates[153] = LocalDate.of(2050, 4, 7);
        dates[154] = LocalDate.of(2050, 7, 7);
        dates[155] = LocalDate.of(2050, 10, 7);
        dates[156] = LocalDate.of(2051, 1, 7);
        dates[157] = LocalDate.of(2051, 4, 7);
        dates[158] = LocalDate.of(2051, 7, 7);
        dates[159] = LocalDate.of(2051, 10, 10);
        dates[160] = LocalDate.of(2052, 1, 10);
        dates[161] = LocalDate.of(2052, 4, 10);
        dates[162] = LocalDate.of(2052, 7, 10);
        dates[163] = LocalDate.of(2052, 10, 7);
        dates[164] = LocalDate.of(2053, 1, 7);
        dates[165] = LocalDate.of(2053, 4, 7);
        dates[166] = LocalDate.of(2053, 7, 7);
        dates[167] = LocalDate.of(2053, 10, 7);
        dates[168] = LocalDate.of(2054, 1, 7);
        dates[169] = LocalDate.of(2054, 4, 7);
        dates[170] = LocalDate.of(2054, 7, 7);
        dates[171] = LocalDate.of(2054, 10, 7);
        dates[172] = LocalDate.of(2055, 1, 7);
        dates[173] = LocalDate.of(2055, 4, 7);
        dates[174] = LocalDate.of(2055, 7, 7);
        dates[175] = LocalDate.of(2055, 10, 7);
        dates[176] = LocalDate.of(2056, 1, 7);
        dates[177] = LocalDate.of(2056, 4, 7);
        dates[178] = LocalDate.of(2056, 7, 7);
        dates[179] = LocalDate.of(2056, 10, 10);
        dates[180] = LocalDate.of(2057, 1, 10);
        dates[181] = LocalDate.of(2057, 4, 10);
        dates[182] = LocalDate.of(2057, 7, 10);
        dates[183] = LocalDate.of(2057, 10, 9);
        dates[184] = LocalDate.of(2058, 1, 9);
        dates[185] = LocalDate.of(2058, 4, 9);
        dates[186] = LocalDate.of(2058, 7, 9);
        dates[187] = LocalDate.of(2058, 10, 7);
        dates[188] = LocalDate.of(2059, 1, 7);
        dates[189] = LocalDate.of(2059, 4, 7);
        dates[190] = LocalDate.of(2059, 7, 7);
        dates[191] = LocalDate.of(2059, 10, 7);
        dates[192] = LocalDate.of(2060, 1, 7);
        dates[193] = LocalDate.of(2060, 4, 7);
        dates[194] = LocalDate.of(2060, 7, 7);
        dates[195] = LocalDate.of(2060, 10, 7);
        dates[196] = LocalDate.of(2061, 1, 7);
        dates[197] = LocalDate.of(2061, 4, 7);
        dates[198] = LocalDate.of(2061, 7, 7);
        dates[199] = LocalDate.of(2061, 10, 7);
        dates[200] = LocalDate.of(2062, 1, 7);
        dates[201] = LocalDate.of(2062, 4, 7);
        dates[202] = LocalDate.of(2062, 7, 7);
        dates[203] = LocalDate.of(2062, 10, 10);
        dates[204] = LocalDate.of(2063, 1, 10);
        dates[205] = LocalDate.of(2063, 4, 10);
        dates[206] = LocalDate.of(2063, 7, 10);
        dates[207] = LocalDate.of(2063, 10, 9);
        dates[208] = LocalDate.of(2064, 1, 9);
        dates[209] = LocalDate.of(2064, 4, 9);
        dates[210] = LocalDate.of(2064, 7, 9);
        dates[211] = LocalDate.of(2064, 10, 7);

        rates[0] = 0.00255501788508306;
        rates[1] = 0.00127750894254153;
        rates[2] = 0.00119931329848555;
        rates[3] = 0.00130363661040443;
        rates[4] = 0.00154262143824825;
        rates[5] = 0.00197283832019138;
        rates[6] = 0.00240966919093781;
        rates[7] = 0.0022998499819357;
        rates[8] = 0.00232107961880112;
        rates[9] = 0.0024878901882511;
        rates[10] = 0.00252598830872147;
        rates[11] = 0.00257438180433315;
        rates[12] = 0.0027531852682539;
        rates[13] = 0.00297658807599608;
        rates[14] = 0.0031900547660693;
        rates[15] = 0.00350423679488787;
        rates[16] = 0.00452287531307616;
        rates[17] = 0.00564160832084155;
        rates[18] = 0.00683357209177279;
        rates[19] = 0.00807873912344805;
        rates[20] = 0.00937529214177954;
        rates[21] = 0.0105993009126465;
        rates[22] = 0.0118353941994927;
        rates[23] = 0.0130724419101699;
        rates[24] = 0.0141516588514675;
        rates[25] = 0.0150581540798364;
        rates[26] = 0.01596453664017;
        rates[27] = 0.0168808164838036;
        rates[28] = 0.0177630179382207;
        rates[29] = 0.0185434699327025;
        rates[30] = 0.0192627121751912;
        rates[31] = 0.0199152802873572;
        rates[32] = 0.0206232644524315;
        rates[33] = 0.0212721793965286;
        rates[34] = 0.0218767609575319;
        rates[35] = 0.0224486442843015;
        rates[36] = 0.0230580641766894;
        rates[37] = 0.0236202916071934;
        rates[38] = 0.0241585074732229;
        rates[39] = 0.0246751043104996;
        rates[40] = 0.0252055552363154;
        rates[41] = 0.0257011399324321;
        rates[42] = 0.0261811224329614;
        rates[43] = 0.026646910839464;
        rates[44] = 0.0271272574416778;
        rates[45] = 0.0275803112060268;
        rates[46] = 0.0280229936214104;
        rates[47] = 0.0284700910780819;
        rates[48] = 0.0289146163785812;
        rates[49] = 0.0293416373771401;
        rates[50] = 0.0297571148194179;
        rates[51] = 0.0301532089095884;
        rates[52] = 0.0305768705396645;
        rates[53] = 0.030981750368075;
        rates[54] = 0.0313822632298305;
        rates[55] = 0.0317788216874987;
        rates[56] = 0.0321791291566317;
        rates[57] = 0.03256352551724;
        rates[58] = 0.0329454934239621;
        rates[59] = 0.0333253263159858;
        rates[60] = 0.0336845067566653;
        rates[61] = 0.034030927612167;
        rates[62] = 0.0343765914570593;
        rates[63] = 0.0347216980004807;
        rates[64] = 0.0350794586089948;
        rates[65] = 0.0354293336207656;
        rates[66] = 0.0357755327939247;
        rates[67] = 0.0361333280279197;
        rates[68] = 0.0364897580204919;
        rates[69] = 0.0368353765558451;
        rates[70] = 0.0371819934088792;
        rates[71] = 0.0375259761302026;
        rates[72] = 0.0378405841348157;
        rates[73] = 0.0381465749518398;
        rates[74] = 0.0384543351703469;
        rates[75] = 0.0387572462653852;
        rates[76] = 0.0390710892624577;
        rates[77] = 0.0393768366224235;
        rates[78] = 0.0396848302030078;
        rates[79] = 0.039995156677711;
        rates[80] = 0.0403084153109465;
        rates[81] = 0.0406174177349243;
        rates[82] = 0.0409256813243392;
        rates[83] = 0.0412366854531077;
        rates[84] = 0.0415493658108351;
        rates[85] = 0.0418547883607967;
        rates[86] = 0.0421632305248611;
        rates[87] = 0.0424747667428431;
        rates[88] = 0.0427869287017732;
        rates[89] = 0.043092169789231;
        rates[90] = 0.0434007417435562;
        rates[91] = 0.0437228899016649;
        rates[92] = 0.0440073927698782;
        rates[93] = 0.0442860342462436;
        rates[94] = 0.0445681476107965;
        rates[95] = 0.0448506877788983;
        rates[96] = 0.0451348491606133;
        rates[97] = 0.0454164558675144;
        rates[98] = 0.0456986371059405;
        rates[99] = 0.0459783235431022;
        rates[100] = 0.0462622363718719;
        rates[101] = 0.0465406775467051;
        rates[102] = 0.0468229536171082;
        rates[103] = 0.0471091257153326;
        rates[104] = 0.0473928425942148;
        rates[105] = 0.0476712451449535;
        rates[106] = 0.047953631006173;
        rates[107] = 0.0482400613308409;
        rates[108] = 0.0485235258847444;
        rates[109] = 0.0488018153393615;
        rates[110] = 0.04908421712733;
        rates[111] = 0.0493707926326169;
        rates[112] = 0.0496581302700772;
        rates[113] = 0.0499434574285483;
        rates[114] = 0.050229916748;
        rates[115] = 0.0505270349870244;
        rates[116] = 0.0508141714950919;
        rates[117] = 0.0510962666329739;
        rates[118] = 0.05138272577673;
        rates[119] = 0.0516672746002701;
        rates[120] = 0.051954340768354;
        rates[121] = 0.0522364582957041;
        rates[122] = 0.0525230293051246;
        rates[123] = 0.0528141174484026;
        rates[124] = 0.0531010281570179;
        rates[125] = 0.0533830735996349;
        rates[126] = 0.0536696500443977;
        rates[127] = 0.0539608216591338;
        rates[128] = 0.0542476085287464;
        rates[129] = 0.0545327436270771;
        rates[130] = 0.0548193521748132;
        rates[131] = 0.0551106242481773;
        rates[132] = 0.0554000268100406;
        rates[133] = 0.0556846407671666;
        rates[134] = 0.0559739430625177;
        rates[135] = 0.0562776150484801;
        rates[136] = 0.0565669519787461;
        rates[137] = 0.0568515558533601;
        rates[138] = 0.0571409010447111;
        rates[139] = 0.0574318475581267;
        rates[140] = 0.0577211396220109;
        rates[141] = 0.0580057457361374;
        rates[142] = 0.0582951384539938;
        rates[143] = 0.0585829696996228;
        rates[144] = 0.058872182814719;
        rates[145] = 0.0591599221741013;
        rates[146] = 0.0594493338080816;
        rates[147] = 0.0597436375140131;
        rates[148] = 0.0600327860500259;
        rates[149] = 0.0603173251074784;
        rates[150] = 0.0606067219675685;
        rates[151] = 0.0609010437589328;
        rates[152] = 0.061190178504637;
        rates[153] = 0.0614747327523647;
        rates[154] = 0.0617641730893943;
        rates[155] = 0.062058566854348;
        rates[156] = 0.0623475929283521;
        rates[157] = 0.0626320647170409;
        rates[158] = 0.0629214450188418;
        rates[159] = 0.0632254303892646;
        rates[160] = 0.0635145104471026;
        rates[161] = 0.0638022266118903;
        rates[162] = 0.0640917200965425;
        rates[163] = 0.0643765787296241;
        rates[164] = 0.0646656527179544;
        rates[165] = 0.0649502077333479;
        rates[166] = 0.065239707717483;
        rates[167] = 0.0655342202840122;
        rates[168] = 0.0658231540902853;
        rates[169] = 0.0661075845715705;
        rates[170] = 0.0663969708653635;
        rates[171] = 0.0666913805426081;
        rates[172] = 0.0669866155436902;
        rates[173] = 0.0672772387791722;
        rates[174] = 0.0675729157070892;
        rates[175] = 0.0678737152371362;
        rates[176] = 0.0681687369157762;
        rates[177] = 0.0684623969359;
        rates[178] = 0.0687578997666716;
        rates[179] = 0.0690683663714657;
        rates[180] = 0.0693635783293975;
        rates[181] = 0.0696541946852992;
        rates[182] = 0.0699498796868121;
        rates[183] = 0.0702474219692317;
        rates[184] = 0.0705425023774571;
        rates[185] = 0.0708329939095628;
        rates[186] = 0.0711285562706328;
        rates[187] = 0.0714227006647057;
        rates[188] = 0.0717178160160016;
        rates[189] = 0.0720083441357388;
        rates[190] = 0.0723039456385875;
        rates[191] = 0.0726046887903935;
        rates[192] = 0.0728998037036392;
        rates[193] = 0.0731935701888994;
        rates[194] = 0.0734891924832347;
        rates[195] = 0.0737899567506222;
        rates[196] = 0.0740851289711288;
        rates[197] = 0.0743757123674795;
        rates[198] = 0.074671368928182;
        rates[199] = 0.0749721664039737;
        rates[200] = 0.0752672035851174;
        rates[201] = 0.075557651875979;
        rates[202] = 0.0758531685887764;
        rates[203] = 0.0761636570662856;
        rates[204] = 0.0764588786952443;
        rates[205] = 0.0767495042921875;
        rates[206] = 0.0770451969532451;
        rates[207] = 0.0773427437300495;
        rates[208] = 0.0776379133547944;
        rates[209] = 0.0779317216490461;
        rates[210] = 0.0782273726953374;
        rates[211] = 0.0785215931059454;

        TCurve tCurve = new TCurve(baseDate, dates, rates, DayCountBasis.ANNUAL_BASIS, DayCount.ACT_365F);
        return tCurve;
    }


}

