package com.nkoad.wallbler.core.implementation.instagram;

import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.RefreshableValidator;
import com.nkoad.wallbler.httpConnector.GETConnector;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

public class InstagramValidator extends RefreshableValidator {
    private static final String USER_PROFILE_URL = "https://graph.instagram.com/me?fields=id,username&access_token=";

    public InstagramValidator(Map<String, Object> properties) {
        super(properties);
    }

    @Override
    public String refreshAccessToken() {
        String newAccessToken = "new access token " + new Date();
        LOGGER.info("got new instagram access token: " + newAccessToken);
        return newAccessToken;
    }

    @Override
    public boolean isAccountValid() {
        try {
            String url = USER_PROFILE_URL + URLEncoder.encode((String) properties.get("accessToken"), "UTF-8");
            HTTPRequest httpRequest = new GETConnector().httpRequest(url);
            if (httpRequest.getStatusCode() == 200) {
                String name = new JSONObject(httpRequest.getBody()).getString("username");
                LOGGER.info("instagram account is valid. account name: " + properties.get("config.name") + ". gotten account name: " + name);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.warn("instagram account is not valid. account name: " + properties.get("config.name"));
        return false;
    }

}
