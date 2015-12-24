package com.anand.analytics.isdamodel.server;

import com.anand.analytics.isdamodel.context.XlServerSpringUtils;
import net.sf.ehcache.CacheManager;
import org.apache.log4j.Logger;
import org.gridgain.grid.GridGain;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Created by Anand on 1/18/2015.
 */
public class CdsServerContextListener
        implements ServletContextListener {
    final Logger logger = Logger.getLogger(CdsServerContextListener.class);

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.debug("Initializing Context");
        XlServerSpringUtils.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("In context destroyed");
        CacheManager.getInstance().shutdown();
        logger.info("Cache Manager shutdown");

        logger.info("Stopping GridManager");
        try {
            GridGain.stop(false);
        } catch (Exception ex) {
            logger.error("Error closing gridManager" + ex);
        }
        logger.info("Grid manager stopped");

        XlServerSpringUtils.close();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                logger.debug(String.format("Deregistering jdbc driver: %s", driver));
            } catch (SQLException e) {
                logger.error(String.format("Error deregistering driver %s", driver), e);
            }
        }
    }
}
