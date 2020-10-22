package com.nkoad.wallbler.core.implementation.facebook;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.WallblerItems;
import com.nkoad.wallbler.httpConnector.GETConnector;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.core.Connector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FacebookConnector extends Connector {
    private static final String FACEBOOK_URL = "https://www.facebook.com/";
    private static final String USERS_API_ACCESS_URL = "https://graph.facebook.com/v6.0/";
    private static final String API_PHOTO_ACCESS_URL = "/photos/uploaded?access_token=";
    private static final String API_POST_ACCESS_URL = "/posts?access_token=";
    private static final String API_VIDEO_ACCESS_URL = "/videos/uploaded?access_token=";
    private static final String API_ALBUM_ACCESS_URL = "/albums?access_token=";
    private static String accountName;
    private static Map<String, FeedType> feedMap = new HashMap<>();

    public FacebookConnector(Map<String, Object> feedProperties, Dictionary<String, Object> accountProperties, Cache cache) {
        super(feedProperties, accountProperties, cache);
        fetchAccountName();
        fillMapForPosts();
        fillMapForPhotos();
        fillMapForVideos();
        fillMapForAlbums();
    }

    @Override
    public void loadData() {
        try {
            String typeOfFeed = (String) feedProperties.get("config.typeOfFeed");
            FeedType feedType = feedMap.get(typeOfFeed);
            String url = feedType.buildFullUrl();
            HTTPRequest httpRequest = new GETConnector().httpRequest(url);
            if (httpRequest.getStatusCode() == 200) {
                List<WallblerItem> wallblerItems = new ArrayList<>();
                JSONArray data = new JSONObject(httpRequest.getBody()).getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject json = data.getJSONObject(i);
                    FacebookWallblerItem item = feedType.retrieveData(json);
                    wallblerItems.add(item);
                }
                cache.add(new WallblerItems(wallblerItems));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillMapForPosts() {
        feedMap.put("posts", new FeedType(API_POST_ACCESS_URL, "permalink_url,full_picture,message,created_time,comments") {
            @Override
            FacebookWallblerItem retrieveData(JSONObject json) throws JSONException {
                FacebookWallblerItem item = new FacebookWallblerItem(feedProperties);
                item.setTitle(accountName);
                item.setUrl(FACEBOOK_URL);
                item.setDate(setDateProperties(json).getTime());
                item.setDescription(setDescriptionProperty(json, "message"));
                item.setLinkToSMPage(json.getString("permalink_url"));
                item.setTypeOfFeed((String) feedProperties.get("config.typeOfFeed"));
                setLikesCommentsSharesProperties(item, json);
                item.generateSocialId();
                return item;
            }
        });
    }

    private void fillMapForPhotos() {
        feedMap.put("photos", new FeedType(API_PHOTO_ACCESS_URL, "link,images,width,name,created_time,comments") {
            @Override
            FacebookWallblerItem retrieveData(JSONObject json) throws JSONException {
                FacebookWallblerItem item = new FacebookWallblerItem(feedProperties);
                item.setTitle("photos");
                item.generateSocialId();
                return item;
            }
        });
    }

    private void fillMapForVideos() {
        feedMap.put("videos", new FeedType(API_VIDEO_ACCESS_URL, "permalink_url,description,updated_time,picture,comments") {
            @Override
            FacebookWallblerItem retrieveData(JSONObject json) throws JSONException {
                FacebookWallblerItem item = new FacebookWallblerItem(feedProperties);
                item.setTitle("videos");
                item.generateSocialId();
                return item;
            }
        });
    }

    private void fillMapForAlbums() {
        feedMap.put("albums", new FeedType(API_ALBUM_ACCESS_URL, "name,link,picture,created_time,comments") {
            @Override
            FacebookWallblerItem retrieveData(JSONObject json) throws JSONException {
                FacebookWallblerItem item = new FacebookWallblerItem(feedProperties);
                item.setTitle("albums");
                item.generateSocialId();
                return item;
            }
        });
    }

    private void fetchAccountName() {
        try {
            String accessToken = URLEncoder.encode((String) accountProperties.get("config.oAuthAccessToken"), "UTF-8");
            String url = USERS_API_ACCESS_URL + accountProperties.get("config.groupId") + "?fields=name,link&access_token=" + accessToken;
            HTTPRequest httpRequest = new GETConnector().httpRequest(url);
            if (httpRequest.getStatusCode() == 200) {
                accountName = new JSONObject(httpRequest.getBody()).getString("name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Date setDateProperties(JSONObject json) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            return sdf.parse(json.getString("created_time"));
        } catch (ParseException | JSONException e) {
            //do nothing
        }
        return null;
    }

    private String setDescriptionProperty(JSONObject json, String s) {
        try {
            return secreteURLsIntoLinks(json.getString(s));
        } catch (JSONException e) {
            // do nothing
        }
        return null;
    }

    private String secreteURLsIntoLinks(String text) {
        StringBuilder result = new StringBuilder();
        for (String item : text.split("\\s+"))
            try {
                URL url = new URL(item);
                result.append("<a href=\"").append(url).append("\"target='_blank'>").append(url).append("</a> ");
            } catch (MalformedURLException e) {
                result.append(item).append(" ");
            }
        return result.toString();
    }

    private void setLikesCommentsSharesProperties(FacebookWallblerItem item, JSONObject json) {
        // todo: need to investigate if we have more than 15 LikesCommentsShares!!!
        try {
            item.setLikedCount(json.getJSONObject("likes").getJSONArray("data").length());
        } catch (JSONException e) {
            item.setLikedCount(0);
        }
        try {
            item.setCommentsCount(json.getJSONObject("comments").getJSONArray("data").length());
        } catch (JSONException e) {
            item.setCommentsCount(0);
        }
        try {
            item.setSharedCount(json.getJSONObject("sharedposts").getJSONArray("data").length());
        } catch (JSONException e) {
            item.setSharedCount(0);
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

        public String buildFullUrl() {
            String accessToken = null;
            try {
                accessToken = URLEncoder.encode((String) accountProperties.get("config.oAuthAccessToken"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return USERS_API_ACCESS_URL + accountProperties.get("config.groupId") + url + accessToken + "&fields=" + fields;
        }
    }

}
