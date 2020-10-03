package com.nkoad.wallbler.core.implementation;

import com.nkoad.wallbler.core.WallblerItemPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class Connector {
    protected final static Logger LOGGER = LoggerFactory.getLogger(Connector.class);
    protected Map<String, Object> feedProperties;
    protected Map<String, Object> accountProperties;

    public Connector(Map<String, Object> feedProperties, Map<String, Object> accountProperties) {
        this.feedProperties = feedProperties;
        this.accountProperties = accountProperties;
    }

    abstract public WallblerItemPack getData();

    public boolean isAccept() {
        boolean accountEnabled = (boolean) accountProperties.get("config.enabled");
        boolean accountValid = (boolean) accountProperties.get("config.valid");
        boolean feedEnabled = (boolean) feedProperties.get("config.enabled");
        return accountEnabled && accountValid && feedEnabled;
    }

}
