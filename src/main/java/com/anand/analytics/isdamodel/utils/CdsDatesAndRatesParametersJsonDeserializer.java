package com.anand.analytics.isdamodel.utils;

import com.anand.analytics.isdamodel.domain.TCurve;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by Anand on 1/18/2015.
 */

public class CdsDatesAndRatesParametersJsonDeserializer implements JsonDeserializer<CdsDatesAndRatesParameters> {

    @Override
    public CdsDatesAndRatesParameters deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            JsonObject jsonObject = json.getAsJsonObject();

            final TCurve curve = Converter.extractCurveHandle(jsonObject, CommonConstants.CURVE);
            final String user = Converter.extractString(jsonObject, CommonConstants.USER);

            return new CdsDatesAndRatesParameters(curve, user);
        } catch (Exception ex) {
            throw new JsonParseException(ex.getMessage());
        }
    }
}
