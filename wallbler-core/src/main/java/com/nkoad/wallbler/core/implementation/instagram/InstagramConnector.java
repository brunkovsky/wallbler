package com.nkoad.wallbler.core.implementation.instagram;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.httpConnector.GETConnector;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.Connector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class InstagramConnector extends Connector {
    private static final String INSTAGRAM_PUBLIC_URL = "https://www.instagram.com/";
    private static final String USER_MEDIA_URL = "https://graph.instagram.com/me/media?fields=caption,id,media_type,media_url,permalink,thumbnail_url,timestamp,username&access_token=";

    public InstagramConnector(Map<String, Object> feedProperties, Map<String, Object> accountProperties, Cache cache) {
        super(feedProperties, accountProperties, cache);
    }

    @Override
    public void loadData() {
        try {
            String url = USER_MEDIA_URL + accountProperties.get("config.accessToken");
            HTTPRequest httpRequest = new GETConnector().httpRequest(url);
            if (httpRequest.getStatusCode() == 200) {
                Set<WallblerItem> wallblerItems = new HashSet<>();
                JSONArray data = new JSONObject(httpRequest.getBody()).getJSONArray("data");
                Date lastRefreshDate = new Date();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject json = data.getJSONObject(i);
                    InstagramWallblerItem item = new InstagramWallblerItem(feedProperties);
                    item.setUrl(INSTAGRAM_PUBLIC_URL + json.getString("username"));
                    item.setTitle(json.getString("username"));
                    item.setDate(setDateProperties(json));
                    item.setLastRefreshDate(lastRefreshDate);
                    item.setLinkToSMPage(json.getString("permalink"));
                    item.setThumbnailUrl(json.getString("media_url"));
                    item.setMediaType(json.getString("media_type"));
                }
                cache.add(wallblerItems);
            }
        } catch (Exception e) {
            LOGGER.error("Instagram Load error ", e);
        }
    }

    private Date setDateProperties(JSONObject json) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .parse(json.getString("timestamp"));
        } catch (ParseException | JSONException e) {
            throw new IllegalArgumentException();
        }
    }

}
