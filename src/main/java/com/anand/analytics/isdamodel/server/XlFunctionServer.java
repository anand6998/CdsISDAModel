package com.anand.analytics.isdamodel.server;

import org.apache.log4j.Logger;
import org.boris.xlloop.FunctionServer;
import org.boris.xlloop.handler.CompositeFunctionHandler;
import org.boris.xlloop.handler.DebugFunctionHandler;
import org.boris.xlloop.handler.FunctionInformationHandler;
import org.boris.xlloop.reflect.ReflectFunctionHandler;

/**
 * Created by aanand on 12/3/2014.
 */
public class XlFunctionServer {
    static final Logger logger = Logger.getLogger(XlFunctionServer.class.getName());

    public void attachShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.debug("Shutting down XlServer");
            }
        });
        logger.debug("Shutdown hook attached");
    }

    public static void main(String[] args) throws Exception {
        final XlFunctionServer xlFunctionServer = new XlFunctionServer();
        xlFunctionServer.attachShutdownHook();

        logger.info("Function Server starting");

        // Create function server on the default port
        final FunctionServer fs = new FunctionServer();

        // Create a reflection function handler and add the Math methods
        final ReflectFunctionHandler rfh = new ReflectFunctionHandler();
        rfh.addMethods("Apollo.", CdsFunctionLibrary.class);

        //rfh.addMethods("Reflect.", Reflect.class);

        // Create a function information handler to register our functions
        final FunctionInformationHandler firh = new FunctionInformationHandler();
        firh.add(rfh.getFunctions());

        logger.info(rfh.getFunctionList());

        // Set the handlers
        final CompositeFunctionHandler cfh = new CompositeFunctionHandler();
        cfh.add(rfh);
        cfh.add(firh);
        fs.setFunctionHandler(new DebugFunctionHandler(cfh));

        // Run the engine
        logger.info("Listening on port " + fs.getPort() + "...");
        fs.run();
    }
}
