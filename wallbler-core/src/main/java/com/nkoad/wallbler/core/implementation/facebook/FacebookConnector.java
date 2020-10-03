package com.nkoad.wallbler.core.implementation.facebook;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.HTTPConnector;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.WallblerItemPack;
import com.nkoad.wallbler.core.implementation.Connector;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class FacebookConnector extends Connector {
    private static final String FACEBOOK_URL = "https://www.facebook.com";
    private static final String USERS_API_ACCESS_URL = "https://graph.facebook.com/v6.0/";
    private static final String API_PHOTO_ACCESS_URL = "/photos/uploaded?access_token=";
    private static final String API_POST_ACCESS_URL = "/posts?access_token=";
    private static final String API_VIDEO_ACCESS_URL = "/videos/uploaded?access_token=";
    private static final String API_ALBUM_ACCESS_URL = "/albums?access_token=";
    private static Map<String, FeedType> feedMap = new HashMap<>();

    public FacebookConnector(Map<String, Object> feedProperties, Map<String, Object> accountProperties, Cache cache) {
        super(feedProperties, accountProperties, cache);

        feedMap.put("posts", new FeedType(API_POST_ACCESS_URL, "permalink_url,full_picture,message,created_time,comments") {
            @Override
            FacebookWallblerItem retrieveData(JSONObject json) throws JSONException {
                FacebookWallblerItem item = new FacebookWallblerItem();
                item.setTitle("posts");
                item.generateSocialId();
                return item;
            }
        });

        feedMap.put("photos", new FeedType(API_PHOTO_ACCESS_URL, "link,images,width,name,created_time,comments") {
            @Override
            FacebookWallblerItem retrieveData(JSONObject json) throws JSONException {
                FacebookWallblerItem item = new FacebookWallblerItem();
                item.setTitle("photos");
                item.generateSocialId();
                return item;
            }
        });

        feedMap.put("videos", new FeedType(API_VIDEO_ACCESS_URL, "permalink_url,description,updated_time,picture,comments") {
            @Override
            FacebookWallblerItem retrieveData(JSONObject json) throws JSONException {
                FacebookWallblerItem item = new FacebookWallblerItem();
                item.setTitle("videos");
                item.generateSocialId();
                return item;
            }
        });

        feedMap.put("albums", new FeedType(API_ALBUM_ACCESS_URL, "name,link,picture,created_time,comments") {
            @Override
            FacebookWallblerItem retrieveData(JSONObject json) throws JSONException {
                FacebookWallblerItem item = new FacebookWallblerItem();
                item.setTitle("albums");
                item.generateSocialId();
                return item;
            }
        });
    }

    @Override
    public void getData() {
        try {
            String url = (String) feedProperties.get("config.url");
            int count = (int) feedProperties.get("config.count");
            HTTPRequest httpRequest = new HTTPConnector().httpGetRequest(url);
            if (httpRequest.getStatusCode() == 200) {
                LOGGER.info("Facebook 200");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    abstract class FeedType {
        String url;
        String fields;

        FeedType(String url, String fields) {
            this.url = url;
            this.fields = fields;
        }

        abstract FacebookWallblerItem retrieveData(JSONObject json) throws JSONException;

        public String buildFullUrl(Map<String, Object> properties) throws UnsupportedEncodingException {
            String accessToken = null;
            try {
                accessToken = URLEncoder.encode((String) properties.get("config.oAuthAccessToken"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return USERS_API_ACCESS_URL + properties.get("config.groupId") + url + accessToken + "&fields=" + fields;
        }
    }

}
