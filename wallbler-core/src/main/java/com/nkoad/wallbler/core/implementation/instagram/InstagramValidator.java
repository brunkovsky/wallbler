package com.nkoad.wallbler.core.implementation.instagram;

import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.RefreshableValidator;
import com.nkoad.wallbler.httpConnector.GETConnector;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class InstagramValidator extends RefreshableValidator {
    private static final String INSTAGRAM_URL = "https://graph.instagram.com/";
    private static final String USER_PROFILE_URL = INSTAGRAM_URL + "me?fields=id,username&access_token=";
    private static final String REFRESH_TOKEN_URL = INSTAGRAM_URL + "refresh_access_token?grant_type=ig_refresh_token&access_token=";
    String screenName;

    public InstagramValidator(Map<String, Object> properties) {
        super(properties);
    }

    @Override
    public String refreshAccessToken() {
        String accessToken = encodedAccessToken((String) accountProperties.get("config.accessToken"));
        try {
            return getNewAccessToken(accessToken);
        } catch (Exception e) {
            LOGGER.warn("could not refresh access token for: " + accountProperties.get("config.name")
            + ". the access token remains the same");
            e.printStackTrace();
        }
        return accessToken;
    }

    @Override
    public boolean isAccountValid() {
        try {
            String accessToken = URLEncoder.encode((String) accountProperties.get("config.accessToken"), "UTF-8");
            screenName = fetchScreenName(accessToken);
            LOGGER.info("instagram account is valid. account name: " + accountProperties.get("config.name"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.warn("instagram account is not valid. account name: " + accountProperties.get("config.name"));
        return false;
    }

    private String encodedAccessToken(String accessToken) {
        String encodedAccessToken = "";
        try {
            encodedAccessToken = URLEncoder.encode(accessToken, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodedAccessToken;
    }

    private String fetchScreenName(String accessToken) throws IOException {
        String url = USER_PROFILE_URL + accessToken;
        HTTPRequest httpRequest = new GETConnector().httpRequest(url);
        if (httpRequest.getStatusCode() == 200) {
            String username = new JSONObject(httpRequest.getBody()).getString("username");
            LOGGER.info("gotten account name: " + username);
            return username;
        }
        LOGGER.warn("can not get account name");
        throw new IOException();
    }

    private String getNewAccessToken(String accessToken) throws IOException {
        String url = REFRESH_TOKEN_URL + accessToken;
        HTTPRequest httpRequest = new GETConnector().httpRequest(url);
        if (httpRequest.getStatusCode() == 200) {
            JSONObject jsonObject = new JSONObject(httpRequest.getBody());
            String access_token = jsonObject.getString("access_token");
            long expiresIn = jsonObject.getLong("expires_in");
            LOGGER.info("got new instagram access token: " + access_token);
            LOGGER.info("got new instagram access token. expires in: " + expiresIn);
            return access_token;
        }
        LOGGER.warn("can not refresh access token");
        throw new IOException();
    }

}
