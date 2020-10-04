package com.nkoad.wallbler.core;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

@Component(name = "OSGIConfig", service = OSGIConfig.class)
public class OSGIConfig {
    private final static Logger LOGGER = LoggerFactory.getLogger(OSGIConfig.class);
    @Reference
    private ConfigurationAdmin configAdmin;

    public ConfigurationAdmin getConfigAdmin() {
        return configAdmin;
    }

    public Map<String, Object> extractAccountProperties(Map<String, Object> feedProperties) {
        String accountName = (String) feedProperties.get("config.accountName");
        String serviceFactoryPid = (String) feedProperties.get("service.factoryPid");
        String accountIdentifier = "(&(config.name=" + accountName + ")(service.factoryPid=" + serviceFactoryPid.replace("Feed", "Account") + "))";
        try {
            Configuration[] configurations = configAdmin.listConfigurations(accountIdentifier);
            if (configurations == null || configurations.length == 0) {
                LOGGER.warn("no account found");
                return new HashMap<>();
            }
            if (configurations.length > 1) {
                LOGGER.warn("non unique account name found");
                return new HashMap<>();
            }
            return Util.dictionaryToMap(configurations[0].getProperties());
        } catch (IOException | InvalidSyntaxException e) {
            e.printStackTrace();
        }
        LOGGER.error("something unexpected happened");
        return new HashMap<>();
    }

    public void setAccessToken(Map<String, Object> accountProperties, String newAccessToken) {
        String factoryPid = (String) accountProperties.get("service.pid");
        Map<String, Object> map = new HashMap<>();
        map.put("config.accessToken", newAccessToken);
        updateProperties(factoryPid, map);
    }

    public void setIsValidTrue(Map<String, Object> accountProperties) {
        setIsValid (accountProperties, true);
    }

    public void setIsValidFalse(Map<String, Object> accountProperties) {
        setIsValid (accountProperties, false);
    }

    private void setIsValid(Map<String, Object> accountProperties, boolean value) {
        String factoryPid = (String) accountProperties.get("service.pid");
        Map<String, Object> map = new HashMap<>();
        map.put("config.valid", value);
        updateProperties(factoryPid, map);
    }

    private void updateProperties(String pid, Map<String, Object> properties) {
        try {
            Configuration configuration = configAdmin.getConfiguration(pid);
            Dictionary<String, Object> props = configuration.getProperties();
            properties.forEach((key, value) -> props.put(key, value != null ? value : ""));
            System.out.println(configuration.getBundleLocation());
            configuration.update(props);
            configuration.setBundleLocation(null); // TODO :
        } catch (IOException e) {
            LOGGER.error("couldn't set properties. pid: " + pid);
        }
    }
}
