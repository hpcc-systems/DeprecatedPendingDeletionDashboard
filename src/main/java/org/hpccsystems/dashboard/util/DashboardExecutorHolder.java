package org.hpccsystems.dashboard.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.WebAppCleanup;
import org.zkoss.zk.ui.util.WebAppInit;

public class DashboardExecutorHolder implements WebAppInit, WebAppCleanup {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DashboardExecutorHolder.class);
    private static volatile ExecutorService executor;

    public static ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public void cleanup(WebApp wapp) throws Exception {
        if (executor != null) {
            executor.shutdown();
            LOGGER.debug("ExecutorService shut down");
        }
    }

    @Override
    public void init(WebApp wapp) throws Exception {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors());
        LOGGER.debug("Initialized an ExecutorService");
    }

}
