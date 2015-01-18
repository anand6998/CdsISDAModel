package com.anand.analytics.isdamodel.context;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Anand on 12/24/2014.
 */
public class XlServerSpringUtils {
    static Logger logger = Logger.getLogger(XlServerSpringUtils.class);
    static ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");

    public static void init() {
        ((ClassPathXmlApplicationContext) applicationContext).registerShutdownHook();
    }

    public static Object getBeanByName(String name) {
        return applicationContext.getBean(name);
    }

    public static void close() {
        try {
            logger.info("Closing spring context");

            ((ClassPathXmlApplicationContext) applicationContext).close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}