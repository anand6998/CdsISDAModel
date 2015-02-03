package com.anand.analytics.isdamodel.utils;

import com.anand.analytics.isdamodel.date.Day;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by Anand on 1/18/2015.
 */
public class CdsIrZeroCurveMakeParametersJsonDeserializer implements JsonDeserializer<CdsIrZeroCurveMakeParameters> {

    @Override
    public CdsIrZeroCurveMakeParameters deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            JsonObject jsonObject = json.getAsJsonObject();
            //region
            Day baseDate = Converter.extractDateFromDouble(jsonObject, CommonConstants.BASE_DATE);
            Day dates[] = Converter.extractDatesFromDoubles(jsonObject, CommonConstants.DATES);
            double[] rates = Converter.extractDoubles(jsonObject, CommonConstants.RATES);

            DayCount dcc = DayCount.ACT_360;
            if (jsonObject.get(CommonConstants.DAY_COUNT) != null)
                dcc = Converter.extractDayCount(jsonObject, CommonConstants.DAY_COUNT);

            DayCountBasis dayCountBasis = DayCountBasis.ANNUAL_BASIS;
            if (jsonObject.get(CommonConstants.BASIS) != null)
                dayCountBasis = ExcelFunctions.xlIntToDayCountBasis(
                        Converter.extractInt(jsonObject, CommonConstants.BASIS)
                );

            String user = "";
            if (jsonObject.get(CommonConstants.USER) != null) {
                user = Converter.extractString(jsonObject, CommonConstants.USER);
            }

            //endregion

            CdsIrZeroCurveMakeParameters cdsIrZeroCurveMakeParameters = new CdsIrZeroCurveMakeParameters(baseDate,
                    dates,
                    rates,
                    dayCountBasis,
                    dcc,
                    user);

            return cdsIrZeroCurveMakeParameters;

        } catch (Exception ex) {
            throw new JsonParseException(ex.getMessage());
        }

    }
}
