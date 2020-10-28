package com.nkoad.wallbler.cache.implementation;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.httpConnector.GETConnector;
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
    private final static String BULK_URL = HOST + "_bulk";
    private final static String SEARCH_URL_TEMPLATE = HOST + "%s/_search?size=%d";
    private final static String DELETE_URL_TEMPLATE = HOST + "%s/_delete_by_query";
    private final static String SEARCH_PAYLOAD = "{\"sort\":[{\"date\":{\"order\":\"desc\"}}]}";
    private final static String DELETE_BY_FEED_NAME_PAYLOAD_TEMPLATE = "{\"query\":{\"match\":{\"feedName\":\"%s\"}}}";
    private final static int MAX_LIMIT = 10000;  // max limit for '_search' in elasticsearch by default
    private final static int WALLBLER_MAX_LIMIT = 25;  // max limit for each social type in the cache

    @Override
    public void add(Set<WallblerItem> wallblerItems) {
        WallblerItem wallblerItem = wallblerItems.stream().findAny().get();
        LOGGER.info("putting data to cache. social: " + wallblerItem.getSocialMediaType() + ". feed name: " + wallblerItem.getFeedName());
        ExistedPosts existedPosts = getExistedPostsAsDateVsSocialId(wallblerItem.getSocialMediaType());
        String payload = generateBulkPayloadForAdding(wallblerItems, new HashSet<>(existedPosts.getRecent().values()));
        if (!payload.isEmpty()) {
            try {
                new POSTConnector() {
                    protected String setContentType() {
                        return "application/x-ndjson";
                    }
                }.httpRequest(BULK_URL, payload);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Collection<Integer> outdatedPosts = existedPosts.getOutdated().values();
        if (!outdatedPosts.isEmpty()) {
            deleteOutdatedPosts(wallblerItem.getSocialMediaType(), outdatedPosts);
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
            String url = String.format(SEARCH_URL_TEMPLATE, Objects.toString(socials, ""), limit);
            HTTPRequest httpRequest = new GETConnector().httpRequest(url, SEARCH_PAYLOAD);
            JSONArray hits = new JSONObject(httpRequest.getBody()).getJSONObject("hits").getJSONArray("hits");
            for (int i = 0; i < hits.length(); i++) {
                result.put(hits.getJSONObject(i).getJSONObject("_source"));
            }
        } catch (FileNotFoundException ignore) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void setAccept(List<WallblerItem> wallblerItems) {
        String payload = generateBulkPayloadForGetting(wallblerItems);
        if (!payload.isEmpty()) {
            try {
                new POSTConnector().httpRequest(BULK_URL, payload);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deletePostsByFeedName(String socialMediaType, String feedName) {
        LOGGER.info("deleting data from cache. socialMediaType: " + socialMediaType + ". feedName: " + feedName);
        try {
            String url = String.format(DELETE_URL_TEMPLATE, socialMediaType);
            String payload = String.format(DELETE_BY_FEED_NAME_PAYLOAD_TEMPLATE, feedName);
            new POSTConnector().httpRequest(url, payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteOutdatedPosts(String socialMediaType, Collection<Integer> outdatedPosts) {
        LOGGER.info("deleting outdatedPosts from cache...");
        try {
            Thread.sleep(3000);
            LOGGER.info("...socialMediaType to delete: " + socialMediaType + ". socialIds: " + outdatedPosts);
            String payload = generateBulkPayloadForDeleting(socialMediaType, outdatedPosts);
            new POSTConnector().httpRequest(BULK_URL, payload);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private ExistedPosts getExistedPostsAsDateVsSocialId(String socialMediaType) {
        JSONArray existedPosts = getData(socialMediaType, MAX_LIMIT);
        ExistedPosts result = new ExistedPosts();
        for (int i = 0; i < existedPosts.length(); i++) {
            JSONObject jsonObject = existedPosts.getJSONObject(i);
            result.put(jsonObject.getLong("date"), jsonObject.getInt("socialId"));
        }
        return result;
    }

    private String generateBulkPayloadForAdding(Set<WallblerItem> wallblerItems, Set<Integer> existedPostsId) {
        StringBuilder payload = new StringBuilder();
        for (WallblerItem wallblerItem : wallblerItems) {
            if (!existedPostsId.contains(wallblerItem.getSocialId())) {
                payload.append("{\"index\":{\"_index\":\"")
                        .append(wallblerItem.getSocialMediaType())
                        .append("\",\"_id\":\"")
                        .append(wallblerItem.getSocialId())
                        .append("\"}}\n")
                        .append(new JSONObject(wallblerItem))
                        .append("\n");
            }
        }
        return payload.toString();
    }

    private String generateBulkPayloadForGetting(List<WallblerItem> wallblerItems) {
        StringBuilder payload = new StringBuilder();
        for (WallblerItem wallblerItem : wallblerItems) {
            payload.append("{\"update\":{\"_index\":\"")
                    .append(wallblerItem.getSocialMediaType())
                    .append("\",\"_id\":\"")
                    .append(wallblerItem.getSocialId())
                    .append("\"}}\n")
                    .append("{\"doc\":{\"accepted\":")
                    .append(wallblerItem.isAccepted())
                    .append("}}\n");
        }
        return payload.toString();
    }

    private String generateBulkPayloadForDeleting(String socialMediaType, Collection<Integer> outdatedPosts) {
        StringBuilder payload = new StringBuilder();
        for (Integer outdatedPost : outdatedPosts) {
            payload.append("{\"delete\":{\"_index\":\"")
                    .append(socialMediaType)
                    .append("\",\"_id\":\"")
                    .append(outdatedPost)
                    .append("\"}}\n");
        }
        return payload.toString();
    }

    static class ExistedPosts {
        private Map<Long, Integer> recent = new HashMap<>();
        private Map<Long, Integer> outdated = new HashMap<>();

        Map<Long, Integer> getRecent() {
            return recent;
        }

        Map<Long, Integer> getOutdated() {
            return outdated;
        }

        void put(Long date, Integer socialId) {
            if (recent.size() < WALLBLER_MAX_LIMIT) {
                recent.put(date, socialId);
            } else {
                outdated.put(date, socialId);
            }
        }
    }

}
