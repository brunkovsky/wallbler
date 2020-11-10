package com.nkoad.wallbler.core;

import java.util.Map;

public abstract class RefreshableValidator extends Validator {

    public RefreshableValidator(Map<String, Object> accountProperties) {
        super(accountProperties);
    }

    public abstract String refreshAccessToken();

    public boolean isAccept() {
        boolean accountEnabled = (boolean) accountProperties.get("config.enabled");
        boolean accountValid = (boolean) accountProperties.get("config.valid");
        return accountEnabled && accountValid;
    }

}
