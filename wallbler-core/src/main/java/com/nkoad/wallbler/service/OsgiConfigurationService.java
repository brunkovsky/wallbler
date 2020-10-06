package com.nkoad.wallbler.service;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.MetaTypeInformation;
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
    private static final String ACCOUNT_FILTER = "(service.factoryPid=com.nkoad.wallbler.core.implementation.*.*Account)";
    private static final String FEED_FILTER = "(service.factoryPid=com.nkoad.wallbler.core.implementation.*.*Feed)";

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
                .filter(a -> a.getSymbolicName().startsWith("com.nkoad.wallbler"))
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

}
