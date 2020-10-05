package com.nkoad.wallbler.core;

import java.util.Map;

public abstract class RefreshableValidator extends Validator {

    public RefreshableValidator(Map<String, Object> properties) {
        super(properties);
    }

    public abstract String refreshAccessToken();

    public boolean isAccept() {
        boolean accountEnabled = (boolean) properties.get("config.enabled");
        boolean accountValid = (boolean) properties.get("config.valid");
        return accountEnabled && accountValid;
    }

}
