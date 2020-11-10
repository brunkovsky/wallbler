package com.nkoad.wallbler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class Validator {
    protected final static Logger LOGGER = LoggerFactory.getLogger(Validator.class);
    protected Map<String, Object> accountProperties;

    public Validator(Map<String, Object> accountProperties) {
        this.accountProperties = accountProperties;
    }

    public abstract boolean isAccountValid();

}
