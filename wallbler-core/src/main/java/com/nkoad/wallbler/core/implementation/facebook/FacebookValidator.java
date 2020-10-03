package com.nkoad.wallbler.core.implementation.facebook;

import com.nkoad.wallbler.core.HTTPConnector;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.implementation.Validator;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Map;

public class FacebookValidator extends Validator {
    private static final String FACEBOOK_URL = "https://www.facebook.com";
    private static final String USERS_API_ACCESS_URL = "https://graph.facebook.com/v6.0/";
    private static final String API_PHOTO_ACCESS_URL = "/photos/uploaded?access_token=";
    private static final String API_POST_ACCESS_URL = "/posts?access_token=";
    private static final String API_VIDEO_ACCESS_URL = "/videos/uploaded?access_token=";
    private static final String API_ALBUM_ACCESS_URL = "/albums?access_token=";
    private static final String API_USERNAME_ACCESS_URL = "?fields=name,link&access_token=";

    public FacebookValidator(Map<String, Object> properties) {
        super(properties);
    }

    @Override
    public boolean isAccountValid() {
        try {
            String url = USERS_API_ACCESS_URL + properties.get("config.groupId") + API_USERNAME_ACCESS_URL + URLEncoder.encode((String) properties.get("config.oAuthAccessToken"), "UTF-8");
            HTTPRequest httpRequest = new HTTPConnector().httpGetRequest(url);
            if (httpRequest.getStatusCode() == 200) {
                LOGGER.debug("facebook account is valid: " + new JSONObject(httpRequest.getBody()).getString("name"));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.debug("facebook account is not valid");
        return false;
    }

}
