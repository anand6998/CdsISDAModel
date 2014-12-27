package com.anand.analytics.isdamodel.context;


import com.anand.analytics.isdamodel.domain.TCurve;
import com.anand.analytics.isdamodel.exception.TCurveNotFoundException;

/**
 * Created by Anand on 12/17/2014.
 */
public class LocalCacheManager implements CdsCacheManager {
    private final ServiceContext serviceContext;

    public LocalCacheManager(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    public void put(String key, TCurve curve) {
        serviceContext.putTCurve(key, curve);
    }

    @Override
    public TCurve get(String key) throws TCurveNotFoundException {
        return serviceContext.getTCurve(key);
    }

    public void clearCache() {
        serviceContext.clearTCurveCache();
    }
}
