package com.nkoad.wallbler.core.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class Feed {
    private final static Logger LOGGER = LoggerFactory.getLogger(Feed.class);
    protected Connector connector;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private ScheduledFuture scheduledFuture;

    protected abstract void assignConnector(Map<String, Object> properties);

    protected void activate(Map<String, Object> properties) {
        LOGGER.info("feed activate: " + properties.get("config.name"));
        assignConnector(properties);
        execute(properties);
    }

    protected void modified(Map<String, Object> properties) {
        LOGGER.info("feed modified: " + properties.get("config.name"));
        deactivate(properties);
        activate(properties);
    }

    protected void deactivate(Map<String, Object> properties) {
        LOGGER.info("feed deactivate: " + properties.get("config.name"));
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        connector.removeFromCache((String) properties.get("service.pid"));
    }

    private void execute(Map<String, Object> properties) {
        Integer delay = (Integer) properties.get("config.delay");
        if (connector.isAccept()) {
            scheduledFuture = executorService.scheduleAtFixedRate(() -> {
                connector.getData();
            }, 1, delay, TimeUnit.SECONDS);
        }
    }

}
