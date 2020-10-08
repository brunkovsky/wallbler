package com.nkoad.wallbler.service;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.MetaTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "OSGi Configuration Service", service = OsgiConfigurationService.class)
public class OsgiConfigurationService {
    @Reference
    private ConfigurationAdmin configAdmin;
    @Reference
    private MetaTypeService metaTypeService;
    private static final Logger LOGGER = LoggerFactory.getLogger(OsgiConfigurationService.class);
    private static final String WALLBLER_PREFIX = "com.nkoad.wallbler";
    private static final String ACCOUNT_FILTER = "(service.factoryPid=" + WALLBLER_PREFIX + ".core.implementation.*.*Account)";
    private static final String FEED_FILTER = "(service.factoryPid=" + WALLBLER_PREFIX + ".core.implementation.*.*Feed)";

    public List<Map<String, Object>> readAccounts() {
        return filterRead(ACCOUNT_FILTER).collect(Collectors.toList());
    }

    public List<Map<String, Object>> readFeeds() {
        return filterRead(FEED_FILTER).collect(Collectors.toList());
    }

    public List<String> getWallblerAccountFactories() {
        return getWallblerFactories().filter(a -> a.endsWith("Account")).collect(Collectors.toList());
    }

    public List<String> getWallblerFeedFactories() {
        return getWallblerFactories().filter(a -> a.endsWith("Feed")).collect(Collectors.toList());
    }

    public Map<String, Object> create(HashMap<String, Object> hashMap) {
        try {
            LOGGER.info("creating the account. name: " + hashMap.get("config.name"));
            String factoryPid = (String) hashMap.get("service.factoryPid");
            Configuration factoryConfiguration = configAdmin.createFactoryConfiguration(factoryPid);
            factoryConfiguration.update(mapToDictionary(hashMap));
            return dictionaryToMap(factoryConfiguration.getProperties());
        } catch (IOException e) {
            LOGGER.error("can not create the account: " + hashMap);
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    private Stream<Map<String, Object>> filterRead(String filter) {
        try {
            return Arrays.stream(Objects.requireNonNull(configAdmin.listConfigurations(filter)))
                    .map(a -> dictionaryToMap((a.getProperties())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Stream.empty();
    }

    private Stream<String> getWallblerFactories() {
        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        return Arrays.stream(bundle.getBundleContext().getBundles())
                .filter(a -> a.getSymbolicName().startsWith(WALLBLER_PREFIX))
                .map(a -> metaTypeService.getMetaTypeInformation(a).getFactoryPids())
                .flatMap(Arrays::stream);
    }

    private <K, V> Map<K, V> dictionaryToMap(Dictionary<K, V> properties) {
        if (properties == null || properties.isEmpty()) {
            return new HashMap<>();
        }
        List<K> keys = Collections.list(properties.keys());
        return keys.stream().collect(Collectors.toMap(Function.identity(), properties::get));
    }

    public static <K, V> Dictionary<K, V> mapToDictionary(Map<K, V> map) {
        Dictionary<K, V> result = new Hashtable<>();
        if (map == null || map.isEmpty()) {
            return result;
        }
        for (Map.Entry<K, V> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
