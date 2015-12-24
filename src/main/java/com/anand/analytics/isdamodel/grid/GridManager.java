package com.anand.analytics.isdamodel.grid;

import com.anand.analytics.isdamodel.domain.TCurve;
import org.apache.log4j.Logger;
import org.gridgain.grid.Grid;
import org.gridgain.grid.GridConfiguration;
import org.gridgain.grid.GridException;
import org.gridgain.grid.GridGain;
import org.gridgain.grid.cache.GridCache;

/**
 * Created by Anand on 1/18/2015.
 */
public class GridManager {

    private final Logger logger = Logger.getLogger(GridManager.class);
    private GridConfiguration gridConfiguration;
    private Grid g;
    private GridCache<String, TCurve> tCurveGridCache;

    public GridManager(GridConfiguration configuration) throws GridException {
        this.gridConfiguration = configuration;
        this.g = GridGain.start(configuration);
        this.tCurveGridCache = g.cache("Cds2Cache");
    }

    public void putX(String curveKey, TCurve tCurve) throws GridException {
        tCurveGridCache.putx(curveKey, tCurve);
    }

    public TCurve get(String curveKey) throws GridException {
        TCurve tCurve = tCurveGridCache.get(curveKey);
        return tCurve;
    }

    public void stop() throws GridException {
        g.stopNodes();
        GridGain.stop(false);
    }
}
