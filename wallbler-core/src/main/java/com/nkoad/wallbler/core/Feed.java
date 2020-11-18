package com.nkoad.wallbler.core;

import com.nkoad.wallbler.activator.Activator;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Feed {
    private final static Logger LOGGER = LoggerFactory.getLogger(Feed.class);
    protected Connector connector;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private ScheduledFuture scheduledFuture;

    protected abstract void assignConnector(Map<String, Object> feedProperties, Map<String, Object> accountProperties);

    protected void activate(Map<String, Object> feedProperties) {
        LOGGER.info("feed activate: " + feedProperties.get("config.name"));
        Map<String, Object> accountProperties = extractAccountProperties(feedProperties);
        if (accountProperties != null) {
            assignConnector(feedProperties, accountProperties);
            execute(feedProperties);
        } else {
            LOGGER.error("can not assign connector for feed name: '" + feedProperties.get("config.name") + "'");
        }
    }

    protected void modified(Map<String, Object> feedProperties) {
        LOGGER.info("feed modified: " + feedProperties.get("config.name"));
        deactivate(feedProperties);
        activate(feedProperties);
    }

    protected void deactivate(Map<String, Object> feedProperties) {
        LOGGER.info("feed deactivate: " + feedProperties.get("config.name"));
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

    protected Map<String, Object> extractAccountProperties(Map<String, Object> feedProperties) {
        String accountName = (String) feedProperties.get("config.accountName");
        String serviceFactoryPid = (String) feedProperties.get("service.factoryPid");
        String accountIdentifier = "(&(config.name=" + accountName + ")(service.factoryPid=" + serviceFactoryPid.replace("Feed", "Account") + "))";
        try {
            Configuration[] configurations = Activator.configAdmin.listConfigurations(accountIdentifier);
            if (configurations == null || configurations.length == 0) {
                LOGGER.error("no account found");
                return null;
            }
            if (configurations.length > 1) {
                LOGGER.error("non unique account name found");
                return null;
            }
            return dictionaryToMap(configurations[0].getProperties());
        } catch (IOException | InvalidSyntaxException e) {
            e.printStackTrace();
        }
        LOGGER.error("something unexpected happened");
        return null; // TODO: to return null???
    }

    private <K, V> Map<K, V> dictionaryToMap(Dictionary<K, V> properties) {
        if (properties == null || properties.isEmpty()) {
            return new HashMap<>();
        }
        List<K> keys = Collections.list(properties.keys());
        return keys.stream().collect(Collectors.toMap(Function.identity(), properties::get));
    }

    private void execute(Map<String, Object> feedProperties) {
        int delayInSeconds = (int) feedProperties.get("config.delay") * 60 * 60;
        if (connector.isAccept()) {
            scheduledFuture = executorService
                    .scheduleAtFixedRate(connector::loadData, 1, delayInSeconds, TimeUnit.SECONDS);
        }
    }

}
