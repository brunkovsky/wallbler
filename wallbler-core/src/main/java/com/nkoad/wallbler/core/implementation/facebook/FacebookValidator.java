package com.nkoad.wallbler.core.implementation.facebook;

import com.nkoad.wallbler.httpConnector.GETConnector;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.Validator;
import org.json.JSONObject;

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
            HTTPRequest httpRequest = new GETConnector().httpRequest(url);
            if (httpRequest.getStatusCode() == 200) {
                String name = new JSONObject(httpRequest.getBody()).getString("name");
                LOGGER.info("facebook account is valid. account name: " + properties.get("config.name") + ". gotten account name: " + name);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.warn("facebook account is not valid. account name: " + properties.get("config.name"));
        return false;
    }

}
