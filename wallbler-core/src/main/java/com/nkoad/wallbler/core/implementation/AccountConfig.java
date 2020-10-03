package com.nkoad.wallbler.core.implementation;

import com.nkoad.wallbler.core.OSGIConfig;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public abstract class AccountConfig <V extends Validator> {
    private final static Logger LOGGER = LoggerFactory.getLogger(AccountConfig.class);
    protected V validator;
    protected OSGIConfig osgiConfig;

    public abstract void assignValidator(Map<String, Object> properties);

    protected void setOsgiConfig(OSGIConfig osgiConfig) {
        this.osgiConfig = osgiConfig;
    }

    protected void activate(Map<String, Object> properties) {
        LOGGER.debug("account activate: " + properties.get("config.name"));
        assignValidator(properties);
        refreshLinkedFeeds(properties);
    }

    protected void modified(Map<String, Object> properties) {
        LOGGER.debug("account modified: " + properties.get("config.name"));
        deactivate(properties);
        activate(properties);
    }

    protected void deactivate(Map<String, Object> properties) {
        LOGGER.debug("account deactivate: " + properties.get("config.name"));
    }

    protected void setValid(Map<String, Object> properties) {
        boolean isAccountCurrentlyValid = (boolean) properties.get("config.valid");
        boolean isAccountRealValid = validator.isAccountValid();
        if (isAccountCurrentlyValid != isAccountRealValid) {
            if (isAccountRealValid) {
                osgiConfig.setIsValidTrue(properties);
            } else {
                osgiConfig.setIsValidFalse(properties);
            }
        }
    }

    private void refreshLinkedFeeds(Map<String, Object> properties) {
        try {
            String accountName = (String) properties.get("config.name");
            String factoryPid = ((String) properties.get("service.factoryPid")).replace("AccountConfig", "FeedConfig");
            String filter = "(&(config.accountName=" + accountName + ")(service.factoryPid=" + factoryPid + "))";
            Configuration[] configurations = osgiConfig.getConfigAdmin().listConfigurations(filter);
            if (configurations != null) {
                for (Configuration configuration : configurations) {
                    LOGGER.debug("refreshing linked feed: " + configuration.getPid());
                    configuration.update(configuration.getProperties());
                }
            }
        } catch (InvalidSyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

}
