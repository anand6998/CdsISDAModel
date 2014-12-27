package com.anand.analytics.isdamodel.context;


import com.anand.analytics.isdamodel.domain.TCurve;
import com.anand.analytics.isdamodel.exception.TCurveNotFoundException;

/**
 * Created by Anand on 12/17/2014.
 */
public interface CdsCacheManager {
    public void put(String key, TCurve curve);
    public TCurve get(String key) throws TCurveNotFoundException;
    public void clearCache();
}
