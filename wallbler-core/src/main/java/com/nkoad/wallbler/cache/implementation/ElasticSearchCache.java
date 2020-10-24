package com.nkoad.wallbler.cache.implementation;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.httpConnector.GETConnector;
import com.nkoad.wallbler.httpConnector.HTTPConnector;
import com.nkoad.wallbler.httpConnector.POSTConnector;
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
    private final static String ADD_BULK_URL = HOST + "_bulk";
    private final static String SEARCH_URL = HOST + "%s/_search?size=%d";
    private final static String UPDATE_URL = HOST + "%s/_doc/%s/_update";
    private final static String REMOVE_URL = HOST + "%s/_delete_by_query";
    private final static String SEARCH_PAYLOAD = "{\"sort\":[{\"date\":{\"order\":\"desc\"}}]}";
    private final static String ACCEPT_PAYLOAD = "{\"doc\":{\"accepted\":%s}}";
    private final static String REMOVE_BY_FEED_NAME_PAYLOAD = "{\"query\":{\"match\":{\"feedName\":\"%s\"}}}";
    private final static int MAX_LIMIT = 10000;  // max limit for '_search' in elasticsearch by default

    @Override
    public void add(Set<WallblerItem> wallblerItems) {
        WallblerItem firstWallblerItem = wallblerItems.stream().findAny().get();
        LOGGER.info("putting data to cache. socials: " + firstWallblerItem.getSocialMediaType() + ". feed name: " + firstWallblerItem.getFeedName());
        Set<Integer> existedPostsId = getExistedPostsId(firstWallblerItem.getSocialMediaType());
        String payload = generateBulkPayload(wallblerItems, existedPostsId);
        try {
            new POSTConnector().httpRequest(ADD_BULK_URL, payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JSONArray getData(String socials, Integer limit) {
        LOGGER.debug("getting data from cache. socials: " + socials + ". limit: " + limit);
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
            LOGGER.info("setting 'accept' field. socialMediaType: " + a.getSocialMediaType() + ". socialId: " + a.getSocialId() + ". accept: " + a.isAccepted());
            try {
                httpConnector.httpRequest(url, payload);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void removeFromCache(String socialMediaType, String feedName) {
        LOGGER.info("removing data from cache. socialMediaType: " + socialMediaType + ". feedName: " + feedName);
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

    private String generateBulkPayload(Set<WallblerItem> wallblerItems, Set<Integer> existedPostsId) {
        StringBuilder payload = new StringBuilder();
        for (WallblerItem wallblerItem : wallblerItems) {
            if (!existedPostsId.contains(wallblerItem.getSocialId())) {
                payload.append("{\"index\":{\"_index\":\"")
                        .append(wallblerItem.getSocialMediaType())
                        .append("\",\"_id\":\"")
                        .append(wallblerItem.getSocialId())
                        .append("\"}}\n")
                        .append(wallblerItem.toString())
                        .append("\n");
            }
        }
        return payload.toString();
    }

//    private void deleteOldPosts()

}
