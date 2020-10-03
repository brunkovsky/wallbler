package com.nkoad.wallbler.core.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class Validator {
    protected final static Logger LOGGER = LoggerFactory.getLogger(Validator.class);
    protected Map<String, Object> properties;

    public Validator(Map<String, Object> properties) {
        this.properties = properties;
    }

    public abstract boolean isAccountValid();

}
