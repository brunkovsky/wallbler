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
        return filterRead(ACCOUNT_FILTER);
    }

    public List<Map<String, Object>> readFeeds() {
        return filterRead(FEED_FILTER);
    }

    public List<String> getWallblerAccountFactories() {
        return getWallblerFactories().filter(a -> a.endsWith("Account")).collect(Collectors.toList());
    }

    public List<String> getWallblerFeedFactories() {
        return getWallblerFactories().filter(a -> a.endsWith("Feed")).collect(Collectors.toList());
    }

    private List<Map<String, Object>> filterRead(String filter) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            Configuration[] configurations = configAdmin.listConfigurations(filter);
            if (configurations != null) {
                for (Configuration configuration : configurations) {
                    Map<String, Object> properties = dictionaryToMap(configuration.getProperties());
                    result.add(properties);
                }
            }
        } catch (IOException | InvalidSyntaxException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Stream<String> getWallblerFactories() {
//        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
//        MetaTypeInformation metaTypeInformation = metaTypeService.getMetaTypeInformation(bundle);
//        return Arrays.stream(metaTypeInformation.getFactoryPids());

        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        BundleContext bundleContext = bundle.getBundleContext();
        Bundle[] bundles = bundleContext.getBundles();
        List<Bundle> result = new ArrayList<>();
        for (Bundle bundleItem : bundles) {
            if (bundleItem.getSymbolicName().startsWith("com.nkoad.wallbler")) {
                result.add(bundleItem);
            }
        }
        System.out.println(result);
        List<String> result2 = new ArrayList<>();
        for (Bundle s : result) {
            MetaTypeInformation metaTypeInformation = metaTypeService.getMetaTypeInformation(s);
            String[] factoryPids = metaTypeInformation.getFactoryPids();
            result2.addAll(Arrays.asList(factoryPids)) ;
        }
        return result2.stream();
    }

    private <K, V> Map<K, V> dictionaryToMap(Dictionary<K, V> properties) {
        if (properties == null || properties.isEmpty()) {
            return new HashMap<>();
        }
        List<K> keys = Collections.list(properties.keys());
        return keys.stream().collect(Collectors.toMap(Function.identity(), properties::get));
    }

}
