package com.nkoad.wallbler.core.implementation.rss;

import com.nkoad.wallbler.core.Validator;

import java.util.Map;

public class RSSValidator extends Validator {

    public RSSValidator(Map<String, Object> properties) {
        super(properties);
    }

    @Override
    public boolean isAccountValid() {
        LOGGER.info("rss account is valid. account name: " + properties.get("config.name"));
        return true;
    }

}
