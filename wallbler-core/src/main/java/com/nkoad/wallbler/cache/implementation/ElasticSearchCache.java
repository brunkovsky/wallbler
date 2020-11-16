package com.nkoad.wallbler.cache.implementation;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.httpConnector.GETConnector;
import com.nkoad.wallbler.httpConnector.POSTConnector;
import com.nkoad.wallbler.httpConnector.POSTConnectorNdjsonContentType;
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
    private final static String SORT_BY_DATE_DESC_PAYLOAD = "\"sort\":[{\"date\":{\"order\":\"desc\"}}]";
    private final static String FILTER_BY_ACCEPTED_FALSE_PAYLOAD = "\"query\":{\"match\":{\"accepted\":false}}";
    private final static String FILTER_BY_ACCEPTED_TRUE_PAYLOAD = "\"query\":{\"match\":{\"accepted\":true}}";
    private final static String FILTER_BY_FEED_NAME_PAYLOAD_TEMPLATE = "\"query\":{\"match_phrase\":{\"feedName\":\"%s\"}}";
    private final static int MAX_LIMIT = 10000;  // max limit for '_search' in elasticsearch by default
    private final static int WALLBLER_MAX_LIMIT = 500;  // define max limit for each social type in the cache

    @Override
    public void add(Set<WallblerItem> wallblerItems) {
        WallblerItem wallblerItem = wallblerItems.stream().findAny().get();
        ExistingPostsIds existingPostsIds = retrieveExistingPostsIds(wallblerItem.getSocialMediaType());
        LOGGER.info("refreshing cache for social: " + wallblerItem.getSocialMediaType()
                + ". feed name: " + wallblerItem.getFeedName());
        String payloadForAdding = generateBulkPayloadForAdding(wallblerItems, existingPostsIds.getRecent());
        if (!payloadForAdding.isEmpty()) {
            try {
                new POSTConnectorNdjsonContentType().httpRequest(BULK_URL, payloadForAdding);
            } catch (IOException e) {
                LOGGER.error("can not add posts");
                e.printStackTrace();
            }
        }
        String payloadForUpdating = generateBulkPayloadForUpdating(wallblerItems, existingPostsIds.getRecent());
        if (!payloadForUpdating.isEmpty()) {
            try {
                new POSTConnectorNdjsonContentType().httpRequest(BULK_URL, payloadForUpdating);
            } catch (IOException e) {
                LOGGER.error("can not update posts");
                e.printStackTrace();
            }
        }
        Set<Integer> outdatedPostsIds = existingPostsIds.getOutdated();
        if (!outdatedPostsIds.isEmpty()) {
            deleteOutdatedPosts(wallblerItem.getSocialMediaType(), outdatedPostsIds);
        }
    }

    @Override
    public JSONArray getAllData(String socials, Integer limit) {
        return getData(socials, limit, "{"
                + SORT_BY_DATE_DESC_PAYLOAD
                + "}");
    }

    @Override
    public JSONArray getAcceptedData(String socials, Integer limit) {
        return getData(socials, limit, "{"
                + SORT_BY_DATE_DESC_PAYLOAD
                + ","
                + FILTER_BY_ACCEPTED_TRUE_PAYLOAD
                + "}");
    }

    @Override
    public JSONArray getNonAcceptedData(String socials, Integer limit) {
        return getData(socials, limit, "{"
                + SORT_BY_DATE_DESC_PAYLOAD
                + ","
                + FILTER_BY_ACCEPTED_FALSE_PAYLOAD
                + "}");
    }

    @Override
    public void setAccept(List<WallblerItem> wallblerItems) {
        String payload = generateBulkPayloadForAccepting(wallblerItems);
        if (!payload.isEmpty()) {
            try {
                new POSTConnectorNdjsonContentType().httpRequest(BULK_URL, payload);
            } catch (IOException e) {
                LOGGER.error("can not set 'accept' to the elasticsearch");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deletePostsByFeedName(String socialMediaType, String feedName) {
        LOGGER.info("deleting data from cache. socialMediaType: " + socialMediaType + ". feedName: " + feedName);
        try {
            String url = String.format(DELETE_URL_TEMPLATE, socialMediaType);
            String payload = String.format("{"
                    + FILTER_BY_FEED_NAME_PAYLOAD_TEMPLATE
                    + "}", feedName);
            new POSTConnector().httpRequest(url, payload);
        } catch (IOException e) {
            LOGGER.error("can not delete posts in the elasticsearch");
            e.printStackTrace();
        }
    }

    private JSONArray getData(String socials, Integer limit, String payload) {
        LOGGER.debug("getting posts from the cache. socials: " + socials + ". limit: " + limit);
        JSONArray result = new JSONArray();
        try {
            if (limit == null || limit < 0 || limit > MAX_LIMIT) {
                limit = MAX_LIMIT;
            }
            String url = String.format(SEARCH_URL_TEMPLATE, Objects.toString(socials, ""), limit);
            HTTPRequest httpRequest = new GETConnector().httpRequest(url, payload);
            JSONArray hits = new JSONObject(httpRequest.getBody()).getJSONObject("hits").getJSONArray("hits");
            for (int i = 0; i < hits.length(); i++) {
                result.put(hits.getJSONObject(i).getJSONObject("_source"));
            }
        } catch (FileNotFoundException ignore) {
        } catch (IOException e) {
            LOGGER.error("can not get posts from the elasticsearch");
            e.printStackTrace();
        }
        return result;
    }

    private void deleteOutdatedPosts(String socialMediaType, Set<Integer> outdatedPostsId) {
        LOGGER.info("deleting outdatedPosts from the cache...");
        try {
            Thread.sleep(3000);
            LOGGER.info("...socialMediaType to delete: " + socialMediaType + ". socialIds: " + outdatedPostsId);
            String payload = generateBulkPayloadForDeleting(socialMediaType, outdatedPostsId);
            new POSTConnectorNdjsonContentType().httpRequest(BULK_URL, payload);
        } catch (InterruptedException | IOException e) {
            LOGGER.error("can not delete outdated posts from the elasticsearch");
            e.printStackTrace();
        }
    }

    private ExistingPostsIds retrieveExistingPostsIds(String socialMediaType) {
        JSONArray existingPosts = getData(socialMediaType, MAX_LIMIT, "{"
                + SORT_BY_DATE_DESC_PAYLOAD
                + "}");
        ExistingPostsIds result = new ExistingPostsIds();
        for (int i = 0; i < existingPosts.length(); i++) {
            JSONObject jsonObject = existingPosts.getJSONObject(i);
            result.add(jsonObject.getInt("socialId"));
        }
        return result;
    }

    private String generateBulkPayloadForAdding(Set<WallblerItem> wallblerItems, Set<Integer> recentPostsIds) {
        StringBuilder payload = new StringBuilder();
        for (WallblerItem wallblerItem : wallblerItems) {
            if (!recentPostsIds.contains(wallblerItem.getSocialId())) {
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

    private String generateBulkPayloadForUpdating(Set<WallblerItem> wallblerItems, Set<Integer> recentPostsIds) {
        StringBuilder payload = new StringBuilder();
        for (WallblerItem wallblerItem : wallblerItems) {
            if (recentPostsIds.contains(wallblerItem.getSocialId())) {
                JSONObject obj = new JSONObject(wallblerItem);
                obj.remove("accepted"); // in order to the 'accepted' field is not changing while updating
                payload.append("{\"update\":{\"_index\":\"")
                        .append(wallblerItem.getSocialMediaType())
                        .append("\",\"_id\":\"")
                        .append(wallblerItem.getSocialId())
                        .append("\"}}\n{\"doc\":")
                        .append(obj)
                        .append("}\n");
            }
        }
        return payload.toString();
    }

    private String generateBulkPayloadForAccepting(List<WallblerItem> wallblerItems) {
        StringBuilder payload = new StringBuilder();
        for (WallblerItem wallblerItem : wallblerItems) {
            payload.append("{\"update\":{\"_index\":\"")
                    .append(wallblerItem.getSocialMediaType())
                    .append("\",\"_id\":")
                    .append(wallblerItem.getSocialId())
                    .append("}}\n{\"doc\":{\"accepted\":")
                    .append(wallblerItem.isAccepted())
                    .append("}}\n");
        }
        return payload.toString();
    }

    private String generateBulkPayloadForDeleting(String socialMediaType, Set<Integer> outdatedPosts) {
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

    static class ExistingPostsIds {
        private Set<Integer> recent = new HashSet<>();
        private Set<Integer> outdated = new HashSet<>();

        Set<Integer> getRecent() {
            return recent;
        }

        Set<Integer> getOutdated() {
            return outdated;
        }

        void add(Integer socialId) {
            if (recent.size() < WALLBLER_MAX_LIMIT) {
                recent.add(socialId);
            } else {
                outdated.add(socialId);
            }
        }
    }

}
