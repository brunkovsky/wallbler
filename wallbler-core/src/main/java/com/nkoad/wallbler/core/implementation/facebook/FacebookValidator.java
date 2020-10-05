package com.nkoad.wallbler.core.implementation.facebook;

import com.nkoad.wallbler.core.HTTPConnectorHelper;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.Validator;

import java.net.URLEncoder;
import java.util.Map;

public class FacebookValidator extends Validator {
    private static final String USERS_API_ACCESS_URL = "https://graph.facebook.com/v6.0/";

    public FacebookValidator(Map<String, Object> properties) {
        super(properties);
    }

    @Override
    public boolean isAccountValid() {
        try {
            String accessToken = URLEncoder.encode((String) properties.get("config.oAuthAccessToken"), "UTF-8");
            String url = USERS_API_ACCESS_URL + properties.get("config.groupId") + "?access_token=" + accessToken;
            HTTPRequest httpRequest = new HTTPConnectorHelper().httpGetRequest(url);
            if (httpRequest.getStatusCode() == 200) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("facebook account is not valid");
        return false;
    }

}
