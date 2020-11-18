package com.nkoad.wallbler.core.implementation.facebook;

import com.nkoad.wallbler.httpConnector.GETConnector;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.Validator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class FacebookValidator extends Validator {
    private static final String USERS_API_ACCESS_URL = "https://graph.facebook.com/v6.0/";
    protected String screenName;
    protected Map<String, String> albums;

    public FacebookValidator(Map<String, Object> accountProperties) {
        super(accountProperties);
    }

    @Override
    public boolean isAccountValid() {
        try {
            String accessToken = URLEncoder.encode((String) accountProperties.get("config.oAuthAccessToken"), "UTF-8");
            screenName = fetchScreenName(accessToken);
            albums = fetchAlbums(accessToken);
            LOGGER.info("facebook account is valid. account name: " + accountProperties.get("config.name")
                    + ". gotten account name: " + screenName
                    + ". albums: " + albums);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.warn("facebook account is not valid. account name: " + accountProperties.get("config.name"));
        return false;
    }

    private String fetchScreenName(String accessToken) throws IOException {
        String url = USERS_API_ACCESS_URL + accountProperties.get("config.groupId") + "?access_token=" + accessToken;
        HTTPRequest httpRequest = new GETConnector().httpRequest(url);
        if (httpRequest.getStatusCode() == 200) {
            return new JSONObject(httpRequest.getBody()).getString("name");
        }
        throw new IOException();
    }

    private Map<String, String> fetchAlbums(String accessToken) throws IOException {
        String url = USERS_API_ACCESS_URL + accountProperties.get("config.groupId") + "/albums?fields=name&access_token=" + accessToken;
        HTTPRequest httpRequest = new GETConnector().httpRequest(url);
        if (httpRequest.getStatusCode() == 200) {
            Map<String, String> albumsMap = new HashMap<>();
            JSONArray data = new JSONObject(httpRequest.getBody()).getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                albumsMap.put(data.getJSONObject(i).getString("name"), data.getJSONObject(i).getString("id"));
            }
            return albumsMap;
        }
        throw new IOException();
    }

}
