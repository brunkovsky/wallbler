package com.nkoad.wallbler.core.implementation.instagram;

import com.nkoad.wallbler.core.implementation.RefreshableValidator;

import java.util.Date;
import java.util.Map;

public class InstagramValidator extends RefreshableValidator {

    public InstagramValidator(Map<String, Object> properties) {
        super(properties);
    }

    @Override
    public String refreshAccessToken() {
        String newAccessToken = "new access token " + new Date();
        LOGGER.debug("got new instagram access token: " + newAccessToken);
        return newAccessToken;
    }

    @Override
    public boolean isAccountValid() {
        return true;
    }

    public boolean isAccept() {
        boolean accountEnabled = (boolean) properties.get("config.enabled");
        boolean accountValid = (boolean) properties.get("config.valid");
        return accountEnabled && accountValid;
    }

}
