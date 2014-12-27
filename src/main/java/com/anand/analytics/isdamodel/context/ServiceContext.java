package com.anand.analytics.isdamodel.context;

import com.anand.analytics.isdamodel.domain.TCurve;
import com.anand.analytics.isdamodel.exception.TCurveNotFoundException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: anand
 * Date: 9/20/14
 * Time: 7:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceContext {

    final static Logger logger = Logger.getLogger(ServiceContext.class);

    final Cache tCurveCache;

    final Lock tCurveLock = new ReentrantLock();

    final CacheManager cacheManger;

    public ServiceContext(CacheManager factoryBean) {
        this.cacheManger = factoryBean;

        tCurveCache = CacheManager.getInstance().getCache("CdsCurveCache");
    }

    public void clearTCurveCache() {
        try {
            logger.info("TCurve cache size (Before clearing) : " + tCurveCache.getKeys().size());
            tCurveLock.lock();
            tCurveCache.removeAll();
            logger.info("TCurve cache size (After clearing) : " + (tCurveCache.getKeys().size()));
        } finally {
            tCurveLock.unlock();
        }
    }

    public TCurve getTCurve(String key) throws TCurveNotFoundException {
        try {
            tCurveLock.lock();
            Element element = tCurveCache.get(key);
            if (element == null) {
                throw new TCurveNotFoundException("Curve not found with key " + key);
            }

            return (TCurve) element.getObjectValue();
        } finally {
            tCurveLock.unlock();
        }
    }

    public void putTCurve(String key, TCurve tCurve) {
        try {
            tCurveLock.lock();
            tCurveCache.put(new Element(key, tCurve));

        } finally {
            tCurveLock.unlock();
        }
    }

    public void shutdown() {
        CacheManager.getInstance().shutdown();
    }
}
