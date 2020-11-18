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

public class FacebookConnector extends Connector<FacebookValidator> {
    private static final String FACEBOOK_URL = "https://www.facebook.com";
    private static final String USERS_API_ACCESS_URL = "https://graph.facebook.com/v8.0/";
    private static final String API_POST_ACCESS_URL = "/posts?access_token=";
    private static final String API_PHOTO_ACCESS_URL = "/photos?type=uploaded&access_token=";
    private static final String API_VIDEO_ACCESS_URL = "/videos/uploaded?access_token=";
    private static Map<String, FeedType> feedMap = new HashMap<>();

    public FacebookConnector(Map<String, Object> feedProperties, Map<String, Object> accountProperties, Cache cache) {
        super(feedProperties, accountProperties, cache);
        postsManaging();
        photosManaging();
        videosManaging();
        validator = new FacebookValidator(accountProperties);
        validator.isAccountValid();
    }

    @Override
    public void loadData() {
        try {
            String typeOfFeed = getFeedPropertyAsString("config.typeOfFeed");
            FeedType feedType = feedMap.get(typeOfFeed);
            String url = feedType.buildFullUrl();
            HTTPRequest httpRequest = new GETConnector().httpRequest(url);
            if (httpRequest.getStatusCode() == 200) {
                Date lastRefreshDate = new Date();
                Set<WallblerItem> wallblerItems = new HashSet<>();
                JSONArray data = new JSONObject(httpRequest.getBody()).getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject json = data.getJSONObject(i);
                    WallblerItem item = feedType.retrieveData(json);
                    item.setLastRefreshDate(lastRefreshDate);
                    item.setUrl(FACEBOOK_URL + "/" + getAccountPropertyAsString("config.groupId"));
                    wallblerItems.add(item);
                }
                cache.add(wallblerItems);
            }
        } catch (Exception e) {
            LOGGER.error("Can't get facebook posts, feed name: '" + feedProperties.get("config.name") + "'", e);
            e.printStackTrace();
        }
    }

    private void postsManaging() {
        feedMap.put("posts", new FeedType(API_POST_ACCESS_URL,
                "permalink_url,full_picture,message,created_time,shares,comments.summary(true).limit(0),likes.summary(true).limit(0),from") {
            @Override
            FacebookWallblerItem retrieveData(JSONObject json) throws JSONException {
                FacebookWallblerItem item = new FacebookWallblerItem(feedProperties);
                item.setDate(extractDateProperties(json));
                item.setTitle(json.getJSONObject("from").getString("name"));
                item.setDescription(extractDescriptionProperty(json, "message"));
                item.setLinkToSMPage(json.getString("permalink_url"));
                setLikesProperty(item, json);
                setCommentsProperty(item, json);
                setSharesProperty(item, json);
                return item;
            }
        });
    }

    private void photosManaging() {
        feedMap.put("photos", new FeedType(API_PHOTO_ACCESS_URL,
                "link,images,name,created_time,comments.summary(true).limit(0),likes.summary(true),from,album") {
            @Override
            FacebookWallblerItem retrieveData(JSONObject json) throws JSONException {
                FacebookWallblerItem item = new FacebookWallblerItem(feedProperties);
                item.setDate(extractDateProperties(json));
                item.setTitle(json.getJSONObject("from").getString("name") + " : " + json.getJSONObject("album").getString("name"));
                item.setDescription(extractDescriptionProperty(json, "name"));
                item.setThumbnailUrl(extractThumbnailUrlProperty(json)); // we still can fetch only first photo from array
                item.setLinkToSMPage(json.getString("link"));
                setLikesProperty(item, json);
                setCommentsProperty(item, json);
                return item;
            }
            @Override
            public String buildFullUrl() {
                String albumName = getFeedPropertyAsString("config.album");
                String photosFrom = albumName == null || albumName.trim().isEmpty() ? getAccountPropertyAsString("config.groupId") : validator.albums.get(albumName);
                return USERS_API_ACCESS_URL + photosFrom + url + retrieveAccessToken() + "&fields=" + fields;
            }
        });
    }

    private void videosManaging() {
        feedMap.put("videos", new FeedType(API_VIDEO_ACCESS_URL,
                "permalink_url,picture,title,description,created_time,comments.summary(true).limit(0),likes.summary(true).limit(0),from") {
            @Override
            FacebookWallblerItem retrieveData(JSONObject json) throws JSONException {
                FacebookWallblerItem item = new FacebookWallblerItem(feedProperties);
                item.setDate(extractDateProperties(json));
                item.setTitle(json.getJSONObject("from").getString("name"));
                item.setDescription(extractDescriptionProperty(json, "name"));
                item.setThumbnailUrl(json.getString("picture"));
                item.setLinkToSMPage(FACEBOOK_URL + json.getString("permalink_url"));
                setLikesProperty(item, json);
                setCommentsProperty(item, json);
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

    private void setLikesProperty(FacebookWallblerItem item, JSONObject json) {
        try {
            item.setLikedCount(json.getJSONObject("likes").getJSONObject("summary").getInt("total_count"));
        } catch (JSONException e) {
            item.setLikedCount(0);
        }
    }

    private void setCommentsProperty(FacebookWallblerItem item, JSONObject json) {
        try {
            item.setCommentsCount(json.getJSONObject("comments").getJSONObject("summary").getInt("total_count"));
        } catch (JSONException e) {
            item.setCommentsCount(0);
        }
    }

    private void setSharesProperty(FacebookWallblerItem item, JSONObject json) {
        try {
            item.setSharedCount(json.getJSONObject("shares").getInt("count"));
        } catch (JSONException e) {
            item.setSharedCount(0);
        }
    }

    private String retrieveAccessToken() {
        String accessToken = null;
        try {
            accessToken = URLEncoder.encode(getAccountPropertyAsString("config.oAuthAccessToken"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return accessToken;
    }

    abstract class FeedType {
        String url;
        String fields;

        FeedType(String url, String fields) {
            this.url = url;
            this.fields = fields;
        }

        abstract WallblerItem retrieveData(JSONObject json) throws JSONException;

        public String buildFullUrl() {
            return USERS_API_ACCESS_URL + getAccountPropertyAsString("config.groupId") + url + retrieveAccessToken() + "&fields=" + fields;
        }
    }

}

/*
действительно ли нам надо вытягивать картинки с Определенных альбомов?
 - да.  тогда нужно придумать механизм определения доступных ID альбомов при валидации аккаунта и потом передать их в качестве опций в фид (сложно)
        или можно просто вставлять albumId в качестве источника откуда вытягивать картинки (легко) (а albumId можно получить из спецмального запроса)
        или можно вставлять имя альбома в качестве источника откуда вытягивать картинки и потом из имени формировать albumId (решаемо)
 - нет. тогда вытягиваются картинки со всей страницы. есть возможность в каждой картинке узнать из какого она альбома


 */