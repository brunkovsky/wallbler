package com.nkoad.wallbler.core;

import com.nkoad.wallbler.cache.definition.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Map;

public abstract class Connector {
    protected final static Logger LOGGER = LoggerFactory.getLogger(Connector.class);
    protected Map<String, Object> feedProperties;
    protected Dictionary<String, Object> accountProperties;
    protected Cache cache;

    public Connector(Map<String, Object> feedProperties, Dictionary<String, Object> accountProperties, Cache cache) {
        this.feedProperties = feedProperties;
        this.accountProperties = accountProperties;
        this.cache = cache;
    }

    protected String getFeedPropertyAsString(String property) {
        return (String) feedProperties.get(property);
    }

    protected Boolean getFeedPropertyAsBoolean(String property) {
        return (Boolean) feedProperties.get(property);
    }

    protected String getAccountPropertyAsString(String property) {
        return (String) accountProperties.get(property);
    }

    abstract public void loadData();

//    public void removeFromCache(String feedPid) {
//        cache.removeFromCache(feedPid);
//    }

    public boolean isAccept() {
        boolean accountEnabled = (boolean) accountProperties.get("config.enabled");
        boolean accountValid = (boolean) accountProperties.get("config.valid");
        boolean feedEnabled = (boolean) feedProperties.get("config.enabled");
        return accountEnabled && accountValid && feedEnabled;
    }

}
