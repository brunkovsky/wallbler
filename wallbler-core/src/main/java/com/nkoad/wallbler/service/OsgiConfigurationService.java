package com.nkoad.wallbler.service;

import com.nkoad.wallbler.exception.AccountAlreadyExistsException;
import com.nkoad.wallbler.exception.ConfigNotFoundException;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.MetaTypeService;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "OSGiConfigurationService", service = OsgiConfigurationService.class)
public class OsgiConfigurationService {
    @Reference
    private ConfigurationAdmin configAdmin;
    @Reference
    private MetaTypeService metaTypeService;
    private static final String WALLBLER_PREFIX = "com.nkoad.wallbler";
    private static final String ACCOUNT_FILTER = "(service.factoryPid=" + WALLBLER_PREFIX + ".core.implementation.*.*Account)";
    private static final String FEED_FILTER = "(service.factoryPid=" + WALLBLER_PREFIX + ".core.implementation.*.*Feed)";

    public Stream<Map<String, Object>> readAccounts() {
        return filterRead(ACCOUNT_FILTER);
    }

    public Stream<Map<String, Object>> readFeeds() {
        return filterRead(FEED_FILTER);
    }

    public Map<String, Object> read(String pid) throws IOException {
        if (!configurationExists(pid)) {
            throw new ConfigNotFoundException(pid);
        }
        return dictionaryToMap(configAdmin.getConfiguration(pid).getProperties());
    }

    public Stream<String> getWallblerAccountFactories() {
        return getWallblerFactories().filter(a -> a.endsWith("Account"));
    }

    public Stream<String> getWallblerFeedFactories() {
        return getWallblerFactories().filter(a -> a.endsWith("Feed"));
    }

    public Stream<Map<String, Object>> getFeedsFromAccount(String pid) throws IOException {
        String name = (String) configAdmin.getConfiguration(pid).getProperties().get("config.name");
        return readFeeds()
                .filter(a -> pid.startsWith(((String) (a.get("service.factoryPid"))).replace("Feed", "Account"))
                        && a.get("config.accountName").equals(name));
    }

    public Map<String, Object> create(HashMap<String, Object> config) throws IOException {
        String factoryPid = (String) config.get("service.factoryPid");
        String name = (String) config.get("config.name");
        if (nameExists(factoryPid, name)) {
            throw new AccountAlreadyExistsException(factoryPid, name);
        }
        Configuration configuration = configAdmin.createFactoryConfiguration(factoryPid);
        setProperties(configuration, mapToDictionary(config));
        return dictionaryToMap(configuration.getProperties());
    }

    public Map<String, Object> update(String pid, HashMap<String, Object> config) throws IOException {
        if (!configurationExists(pid)) {
            throw new ConfigNotFoundException(pid);
        }
        Configuration configuration = configAdmin.getConfiguration(pid);
        setProperties(configuration, mapToDictionary(config));
        return dictionaryToMap(configuration.getProperties());
    }

    public void delete(String pid) throws IOException {
        if (!configurationExists(pid)) {
            throw new ConfigNotFoundException(pid);
        }
        configAdmin.getConfiguration(pid).delete();
    }

    private boolean nameExists(String factoryPid, String name) {
        try {
            String filter = String.format("(&(config.name=%s)(service.factoryPid=%s))", name, factoryPid);
            Configuration[] configurations = configAdmin.listConfigurations(filter);
            return configurations != null && configurations.length > 0;
        } catch (IOException | InvalidSyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean configurationExists(String pid) throws IOException {
        Configuration configuration = configAdmin.getConfiguration(pid);
        return configuration.getFactoryPid() != null;
    }

    private void setProperties(Configuration configuration, Dictionary<String, Object> properties) throws IOException {
        configuration.update(properties);
        configuration.setBundleLocation(null);
    }

    private Stream<Map<String, Object>> filterRead(String filter) {
        try {
            return Arrays.stream(Objects.requireNonNull(configAdmin.listConfigurations(filter)))
                    .map(a -> dictionaryToMap((a.getProperties())));
        } catch (Exception e) {
            // it's ok. just no configurations
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

    private <K, V> Dictionary<K, V> mapToDictionary(Map<K, V> map) {
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
