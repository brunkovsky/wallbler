package com.nkoad.wallbler.cache.implementation;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.core.WallblerItems;
import com.nkoad.wallbler.httpConnector.GETConnector;
import com.nkoad.wallbler.httpConnector.HTTPConnector;
import com.nkoad.wallbler.httpConnector.POSTConnector;
import com.nkoad.wallbler.httpConnector.PUTConnector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Component(name = "ElasticSearchCache", service = Cache.class)
public class ElasticSearchCache implements Cache {
    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticSearchCache.class);
    private final static String HOST = "http://localhost:9200/";
    private final static String ADD_URL = HOST + "%s/_doc/%s";
    private final static String SEARCH_URL = HOST + "%s/_search?size=%d";
    private final static String UPDATE_URL = HOST + "%s/_doc/%s/_update";
    private final static String REMOVE_URL = HOST + "%s/_delete_by_query";
    private final static String SEARCH_PAYLOAD = "{\"sort\":[{\"date\":{\"order\":\"desc\"}}]}";
    private final static String ACCEPT_PAYLOAD = "{\"doc\":{\"accepted\":%s}}";
    private final static String REMOVE_BY_FEED_NAME_PAYLOAD = "{\"query\":{\"match\":{\"feedName\":\"%s\"}}}";
    private final static int MAX_LIMIT = 10000;  // max limit for '_search' in elastic search

    @Override
    public void add(WallblerItems wallblerItems) {
        WallblerItem firstWallblerItem = wallblerItems.getData().get(0);
        LOGGER.info("got new data. feed name: " + firstWallblerItem.getFeedName());
        String socialMediaType = firstWallblerItem.getSocialMediaType();
        Set<Integer> existedPostsId = getExistedPostsId(socialMediaType);
        long lastRefreshDate = wallblerItems.getLastRefreshDate();
        HTTPConnector httpConnector = new PUTConnector();
        wallblerItems.getData().stream()
                .filter(a -> !existedPostsId.contains(a.getSocialId()))
                .forEach(a -> {
                    String url = String.format(ADD_URL, socialMediaType, a.getSocialId());
                    JSONObject jsonObject = new JSONObject(a);
                    jsonObject.put("lastRefreshDate", lastRefreshDate);
                    String payload = jsonObject.toString();
                    try {
                        httpConnector.httpRequest(url, payload);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public JSONArray getData(String socials, Integer limit) {
        JSONArray result = new JSONArray();
        try {
            if (limit == null || limit < 0 || limit > MAX_LIMIT) {
                limit = MAX_LIMIT;
            }
            String url = String.format(SEARCH_URL, Objects.toString(socials, ""), limit);
            HTTPRequest httpRequest = new GETConnector().httpRequest(url, SEARCH_PAYLOAD);
            JSONArray hits = new JSONObject(httpRequest.getBody()).getJSONObject("hits").getJSONArray("hits");
            for (int i = 0; i < hits.length(); i++) {
                result.put(hits.getJSONObject(i).getJSONObject("_source"));
            }
        }
        catch (FileNotFoundException ignore) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void setAccept(List<WallblerItem> wallblerItems) {
        HTTPConnector httpConnector = new POSTConnector();
        wallblerItems.forEach(a -> {
            String url = String.format(UPDATE_URL, a.getSocialMediaType(), a.getSocialId());
            String payload = String.format(ACCEPT_PAYLOAD, a.isAccepted());
            try {
                httpConnector.httpRequest(url, payload);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void removeFromCache(String socialMediaType, String feedName) {
        try {
            String url = String.format(REMOVE_URL, socialMediaType);
            String payload = String.format(REMOVE_BY_FEED_NAME_PAYLOAD, feedName);
            new POSTConnector().httpRequest(url, payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<Integer> getExistedPostsId(String socialMediaType) {
        JSONArray existedPosts = getData(socialMediaType, MAX_LIMIT);
        Set<Integer> result = new HashSet<>();
        for (int i = 0; i < existedPosts.length(); i++) {
            result.add(existedPosts.getJSONObject(i).getInt("socialId"));
        }
        return result;
    }

}
