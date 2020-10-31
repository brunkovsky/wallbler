package com.nkoad.wallbler.core.implementation.facebook;

import com.nkoad.wallbler.cache.definition.Cache;
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
    private static final String USERS_API_ACCESS_URL = "https://graph.facebook.com/v8.0/";
    private static final String API_PHOTO_ACCESS_URL = "/photos?type=uploaded&access_token=";
    private static final String API_POST_ACCESS_URL = "/posts?access_token=";
    private static final String API_VIDEO_ACCESS_URL = "/videos/uploaded?access_token=";
    private static final String API_ALBUM_ACCESS_URL = "/albums?access_token=";
    private static Map<String, FeedType> feedMap = new HashMap<>();

    public FacebookConnector(Map<String, Object> feedProperties, Dictionary<String, Object> accountProperties, Cache cache) {
        super(feedProperties, accountProperties, cache);
        postsManaging();
        photosManaging();
        videosManaging();
        albumsManaging();
    }

    @Override
    public void loadData() {
        try {
            String typeOfFeed = (String) feedProperties.get("config.typeOfFeed");
            FeedType feedType = feedMap.get(typeOfFeed);
            String url = feedType.buildFullUrl();
            HTTPRequest httpRequest = new GETConnector().httpRequest(url);
            if (httpRequest.getStatusCode() == 200) {
                long lastRefreshDate = new Date().getTime();
                Set<WallblerItem> wallblerItems = new HashSet<>();
                JSONArray data = new JSONObject(httpRequest.getBody()).getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject json = data.getJSONObject(i);
                    FacebookWallblerItem item = feedType.retrieveData(json);
                    item.setLastRefreshDate(lastRefreshDate);
                    item.setUrl(FACEBOOK_URL);
                    wallblerItems.add(item);
                }
                cache.add(wallblerItems);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postsManaging() {
        feedMap.put("posts", new FeedType(API_POST_ACCESS_URL, "permalink_url,full_picture,message,created_time,shares,comments.summary(true).limit(0),likes.summary(true).limit(0),from") {
            @Override
            FacebookWallblerItem retrieveData(JSONObject json) throws JSONException {
                FacebookWallblerItem item = new FacebookWallblerItem(feedProperties);
                item.setDate(extractDateProperties(json).getTime());
                item.setTitle(json.getJSONObject("from").getString("name"));
                item.setDescription(extractDescriptionProperty(json, "message"));
                item.setLinkToSMPage(json.getString("permalink_url"));
                item.setTypeOfFeed((String) feedProperties.get("config.typeOfFeed"));
                setLikesCommentsSharesProperties(item, json);
                item.generateSocialId();
                return item;
            }
        });
    }

    private void photosManaging() {
        feedMap.put("photos", new FeedType(API_PHOTO_ACCESS_URL, "link,images,name,created_time,comments.summary(true).limit(0),likes.summary(true),from,album") {
            @Override
            FacebookWallblerItem retrieveData(JSONObject json) throws JSONException {
                FacebookWallblerItem item = new FacebookWallblerItem(feedProperties);
                item.setDate(extractDateProperties(json).getTime());
                item.setTitle(json.getJSONObject("from").getString("name"));
                item.setDescription(extractDescriptionProperty(json, "name"));
                item.setThumbnailUrl(extractThumbnailUrlProperty(json));
                item.setLinkToSMPage(json.getString("link"));
                item.setTypeOfFeed((String) feedProperties.get("config.typeOfFeed"));
                setLikesCommentsSharesProperties(item, json);
                item.generateSocialId();
                return item;
            }
        });
    }

    private void videosManaging() {
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

    private void albumsManaging() {
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

    private Date extractDateProperties(JSONObject json) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            return sdf.parse(json.getString("created_time"));
        } catch (ParseException | JSONException ignore) {
        }
        return null;
    }

    private String extractThumbnailUrlProperty(JSONObject json) {
        return json.getJSONArray("images").getJSONObject(0).getString("source");
    }

    private String extractDescriptionProperty(JSONObject json, String s) {
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
            item.setLikedCount(json.getJSONObject("likes").getJSONObject("summary").getInt("total_count"));
        } catch (JSONException e) {
            item.setLikedCount(0);
        }
        try {
            item.setCommentsCount(json.getJSONObject("comments").getJSONObject("summary").getInt("total_count"));
        } catch (JSONException e) {
            item.setCommentsCount(0);
        }
        try {
            item.setSharedCount(json.getJSONObject("shares").getInt("count"));
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
