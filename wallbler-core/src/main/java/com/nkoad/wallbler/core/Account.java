package com.nkoad.wallbler.core;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public abstract class Account<V extends Validator> { // TODO : try to extend OSGiConfigurationService
    private final static Logger LOGGER = LoggerFactory.getLogger(Account.class);
    protected V validator;

    public abstract void assignValidator(Map<String, Object> accountProperties);

    protected void activate(Map<String, Object> accountProperties) {
        LOGGER.info("account activate: '" + accountProperties.get("config.name") + "'");
        assignValidator(accountProperties);
        refreshLinkedFeeds(accountProperties);
    }

    protected void modified(Map<String, Object> accountProperties) {
        LOGGER.info("account modified: '" + accountProperties.get("config.name") + "'");
        deactivate(accountProperties);
        activate(accountProperties);
    }

    protected void deactivate(Map<String, Object> accountProperties) {
        LOGGER.info("account deactivate: '" + accountProperties.get("config.name") + "'");
    }

    protected void setValid(Map<String, Object> accountProperties) {
        boolean isAccountValidState = (boolean) accountProperties.get("config.valid");
        boolean isAccountReallyValid = validator.isAccountValid();
        if (isAccountValidState != isAccountReallyValid) {
            setIsValid(accountProperties, isAccountReallyValid);
        }
    }

    void refreshLinkedFeeds(Map<String, Object> accountProperties) {
        try {
            String accountName = (String) accountProperties.get("config.name");
            String factoryPid = ((String) accountProperties.get("service.factoryPid")).replace("Account", "Feed");
            String filter = "(&(config.accountName=" + accountName + ")(service.factoryPid=" + factoryPid + "))";
            Configuration[] configurations = getConfigAdmin().listConfigurations(filter);
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

    void updateProperties(String pid, Map<String, Object> accountProperties) {
        try {
            Configuration configuration = getConfigAdmin().getConfiguration(pid);
            Dictionary<String, Object> props = configuration.getProperties();
            accountProperties.forEach((key, value) -> props.put(key, value != null ? value : ""));
            configuration.update(props);
            configuration.setBundleLocation(null); // TODO :
        } catch (IOException e) {
            LOGGER.error("couldn't set properties. pid: " + pid);
        }
    }

    private void setIsValid(Map<String, Object> accountProperties, boolean value) {
        String pid = (String) accountProperties.get("service.pid");
        Map<String, Object> map = new HashMap<>();
        map.put("config.valid", value);
        updateProperties(pid, map);
    }

    private ConfigurationAdmin getConfigAdmin() {
        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        BundleContext bundleContext = bundle.getBundleContext();
        ServiceReference ref = bundleContext.getServiceReference(ConfigurationAdmin.class);
        return (ConfigurationAdmin) bundleContext.getService(ref);
    }

//    private OSGiConfigurationService getOSGiConfigurationService() {
//        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
//        BundleContext bundleContext = bundle.getBundleContext();
//        ServiceReference ref = bundleContext.getServiceReference(OSGiConfigurationService.class);
//        return (OSGiConfigurationService) bundleContext.getService(ref);
//    }

}
