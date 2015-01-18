package com.anand.analytics.isdamodel.server;

import com.anand.analytics.isdamodel.context.XlServerSpringUtils;
import com.anand.analytics.isdamodel.domain.TCurve;
import com.anand.analytics.isdamodel.utils.CdsDatesAndRatesParameters;
import com.anand.analytics.isdamodel.utils.CdsDatesAndRatesParametersJsonDeserializer;
import com.anand.analytics.isdamodel.utils.CdsIrZeroCurveMakeParameters;
import com.anand.analytics.isdamodel.utils.CdsIrZeroCurveMakeParametersJsonDeserializer;
import com.anand.analytics.isdamodel.utils.CommonConstants;
import com.anand.analytics.isdamodel.utils.DayCount;
import com.anand.analytics.isdamodel.utils.DayCountBasis;
import com.anand.analytics.isdamodel.utils.ExcelFunctions;
import com.anand.analytics.isdamodel.utils.StringReturnType;
import com.anand.analytics.isdamodel.utils.TCurveIdGenerator;
import com.anand.analytics.isdamodel.utils.TwodDoubleArrayReturnType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.gridgain.grid.Grid;
import org.gridgain.grid.cache.GridCache;
import org.threeten.bp.LocalDate;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anand on 1/18/2015.
 */
@Path("/cds")
public class CdsServices {

    private final Logger logger = Logger.getLogger(CdsServices.class);
    private final GsonBuilder gsonBuilder = new GsonBuilder();

    private final Grid grid;
    private final GridCache<String, TCurve> tCurveGridCache;

    public CdsServices() {
        logger.info("CdsServices Initializing");
        gsonBuilder.registerTypeAdapter(CdsIrZeroCurveMakeParameters.class, new CdsIrZeroCurveMakeParametersJsonDeserializer());
        gsonBuilder.registerTypeAdapter(CdsDatesAndRatesParameters.class, new CdsDatesAndRatesParametersJsonDeserializer());
        grid = (Grid) XlServerSpringUtils.getBeanByName("gridGainBean");
        tCurveGridCache = grid.cache("Cds2Cache");
    }

    @GET
    @Path("/echoString")
    public String echoString(@QueryParam("input") String input) {
        logger.info("echoString : " + input);
        return input;
    }

    @POST
    @Path("/cdsIrZeroCurveMake")
    @Produces("text/plain")
    @Consumes("application/x-www-form-urlencoded")
    public String cdsIrZeroCurveMake(String jsonObject) {
        final StringReturnType returnType = new StringReturnType();
        final Gson gson = gsonBuilder.create();
        try {
            final JsonParser parser = new JsonParser();
            final JsonObject obj = (JsonObject) parser.parse(jsonObject);
            //System.out.println(obj);

            final CdsIrZeroCurveMakeParameters cdsIrZeroCurveMakeParameters
                    = gson.fromJson(obj, CdsIrZeroCurveMakeParameters.class);

            final String user = cdsIrZeroCurveMakeParameters.user;
            final LocalDate baseDate = cdsIrZeroCurveMakeParameters.baseDate;
            final LocalDate[] dates = cdsIrZeroCurveMakeParameters.dates;
            final double[] rates = cdsIrZeroCurveMakeParameters.rates;
            final DayCount dayCount = cdsIrZeroCurveMakeParameters.dayCount;
            final DayCountBasis dayCountBasis = cdsIrZeroCurveMakeParameters.basis;

            final TCurve tCurve = new TCurve(baseDate,
                    dates,
                    rates,
                    dayCountBasis,
                    dayCount);

            final String tCurveKey = TCurveIdGenerator.id("CdsServices.cdsIrZeroCurveMake");

            logger.info("tCurveKey : " + tCurveKey);
            tCurveGridCache.putx(tCurveKey, tCurve);
            returnType.returnType = CommonConstants.SUCCESS;
            returnType.value = tCurveKey;

            logger.info(user + ": irZeroCurveMake : " + tCurveKey);

        } catch (NullPointerException ex) {
            returnType.returnType = CommonConstants.FAILURE;
            returnType.errorString = ex.toString();
            logger.error(ex.getMessage());
            logger.error(jsonObject);
        } catch (Exception ex) {
            returnType.returnType = CommonConstants.FAILURE;
            returnType.errorString = ex.getMessage();
            logger.error(ex.getMessage());
            logger.error(jsonObject);
        }

        return gson.toJson(returnType);
    }


    @POST
    @Path("/cdsDatesAndRates")
    @Produces("text/plain")
    @Consumes("application/x-www-form-urlencoded")
    public String cdsDatesAndRates(String jsonObject) {
        final TwodDoubleArrayReturnType returnType = new TwodDoubleArrayReturnType();
        final Gson gson = gsonBuilder.create();

        try {
            final JsonParser parser = new JsonParser();
            final JsonObject obj = (JsonObject) parser.parse(jsonObject);

            final CdsDatesAndRatesParameters parameters
                    = gson.fromJson(obj, CdsDatesAndRatesParameters.class);
            final String user = parameters.user;
            final TCurve curve = parameters.curve;

            final LocalDate[] dates = curve.getDates();
            final double[] rates = curve.getRates();

            final List<double[]> retArray = new ArrayList<double[]>(dates.length);

            for (int i = 0; i < dates.length; i++) {
                final double xlDate = ExcelFunctions.localDateToExcelDate(dates[i]);
                final double rate = rates[i];

                final double[] row = new double[2];
                row[0] = xlDate;
                row[1] = rate;

                retArray.add(row);
            }

            returnType.returnType = CommonConstants.SUCCESS;
            returnType.values = retArray;

            logger.info(user + " : cdsDatesAndRates : " + retArray);

        } catch (Exception ex) {
            returnType.errorString = ex.getMessage();
            returnType.returnType = CommonConstants.FAILURE;
        }

        return gson.toJson(returnType);
    }

}

