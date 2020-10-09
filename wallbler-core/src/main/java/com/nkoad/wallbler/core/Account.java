package com.nkoad.wallbler.core;

import com.nkoad.wallbler.activator.Activator;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public abstract class Account<V extends Validator> {
    private final static Logger LOGGER = LoggerFactory.getLogger(Account.class);
    protected V validator;

    public abstract void assignValidator(Map<String, Object> properties);

    protected void activate(Map<String, Object> properties) {
        LOGGER.info("account activate: " + properties.get("config.name"));
        assignValidator(properties);
        refreshLinkedFeeds(properties);
    }

    protected void modified(Map<String, Object> properties) {
        LOGGER.info("account modified: " + properties.get("config.name"));
        deactivate(properties);
        activate(properties);
    }

    protected void deactivate(Map<String, Object> properties) {
        LOGGER.info("account deactivate: " + properties.get("config.name"));
    }

    protected void setValid(Map<String, Object> properties) {
        boolean isAccountCurrentlyValid = (boolean) properties.get("config.valid");
        boolean isAccountRealValid = validator.isAccountValid();
        if (isAccountCurrentlyValid != isAccountRealValid) {
            setIsValid(properties, isAccountRealValid);
        }
    }

    void setAccessToken(Map<String, Object> accountProperties, String newAccessToken) {
        String factoryPid = (String) accountProperties.get("service.pid");
        Map<String, Object> map = new HashMap<>();
        map.put("config.accessToken", newAccessToken);
        updateProperties(factoryPid, map);
    }

    private void refreshLinkedFeeds(Map<String, Object> properties) {
        try {
            String accountName = (String) properties.get("config.name");
            String factoryPid = ((String) properties.get("service.factoryPid")).replace("Account", "Feed");
            String filter = "(&(config.accountName=" + accountName + ")(service.factoryPid=" + factoryPid + "))";
            Configuration[] configurations = Activator.configAdmin.listConfigurations(filter);
            if (configurations != null) {
                for (Configuration configuration : configurations) {
                    LOGGER.info("refreshing linked feed: " + configuration.getPid());
                    configuration.update(configuration.getProperties());
                }
            }
        } catch (InvalidSyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    private void setIsValid(Map<String, Object> accountProperties, boolean value) {
        String factoryPid = (String) accountProperties.get("service.pid");
        Map<String, Object> map = new HashMap<>();
        map.put("config.valid", value);
        updateProperties(factoryPid, map);
    }

    private void updateProperties(String pid, Map<String, Object> properties) {
        try {
            Configuration configuration = Activator.configAdmin.getConfiguration(pid);
            Dictionary<String, Object> props = configuration.getProperties();
            properties.forEach((key, value) -> props.put(key, value != null ? value : ""));
            configuration.update(props);
            configuration.setBundleLocation(null); // TODO :
        } catch (IOException e) {
            LOGGER.error("couldn't set properties. pid: " + pid);
        }
    }

}
