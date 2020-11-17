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
import java.util.stream.Collectors;

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
    private final static int WALLBLER_MAX_LIMIT = 100;  // define max limit for each social type in the cache

    @Override
    public void add(Set<WallblerItem> wallblerItems) {
        WallblerItem wallblerItem = wallblerItems.stream().findAny().get();
        String socialMediaType = wallblerItem.getSocialMediaType();
        String feedName = wallblerItem.getFeedName();
        ExistingPosts existingPosts = retrieveExistingPosts(socialMediaType);
        LOGGER.info("refreshing cache for social: " + socialMediaType
                + ". feed name: " + feedName + "\n"
                + "existing posts quantity: " + (existingPosts.recent.size() + existingPosts.outdated.size()) + "\n"
                + "recent posts quantity: " + existingPosts.recent.size() + "\n"
                + "outdated posts quantity: " + existingPosts.outdated.size());

        Payload payloadForAdding = generateBulkPayloadForAdding(wallblerItems, existingPosts.getRecentAsIds());
        if (payloadForAdding.quantity > 0) {
            LOGGER.info("adding social: " + socialMediaType + " to cache"
                    + ". feed name: " + feedName
                    + ". quantity to add: " + payloadForAdding.quantity);
            try {
                new POSTConnectorNdjsonContentType().httpRequest(BULK_URL, payloadForAdding.payload);
            } catch (IOException e) {
                LOGGER.error("can not add posts");
                e.printStackTrace();
            }
        }

        Payload payloadForUpdating = generateBulkPayloadForUpdating(wallblerItems, existingPosts.getRecentAsIds());
        if (payloadForUpdating.quantity > 0) {
            LOGGER.info("updating social: " + socialMediaType
                    + ". feed name: " + feedName
                    + ". quantity to update: " + payloadForUpdating.quantity);
            try {
                new POSTConnectorNdjsonContentType().httpRequest(BULK_URL, payloadForUpdating.payload);
            } catch (IOException e) {
                LOGGER.error("can not update posts");
                e.printStackTrace();
            }
        }

        Payload payloadForDeleting = generateBulkPayloadForDeleting(socialMediaType, existingPosts.getOutdatedAsIds());
        if (payloadForDeleting.quantity > 0) {
            LOGGER.info("deleting outdated posts for social: " + socialMediaType
                    + ". feed name: " + feedName
                    + ". quantity to delete: " + payloadForDeleting.quantity);
            try {
                Thread.sleep(3000);
                new POSTConnectorNdjsonContentType().httpRequest(BULK_URL, payloadForDeleting.payload);
            } catch (Exception e) {
                LOGGER.error("can not delete posts");
                e.printStackTrace();
            }
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

    private ExistingPosts retrieveExistingPosts(String socialMediaType) {
        JSONArray existingPosts = getData(socialMediaType, MAX_LIMIT, "{"
                + SORT_BY_DATE_DESC_PAYLOAD
                + "}");
        ExistingPosts result = new ExistingPosts();
        for (int i = 0; i < existingPosts.length(); i++) {
            JSONObject jsonObject = existingPosts.getJSONObject(i);
            result.add(jsonObject);
        }
        return result;
    }

    private Payload generateBulkPayloadForAdding(Set<WallblerItem> wallblerItems, Set<Integer> recentPostsIds) {
        StringBuilder payload = new StringBuilder();
        int count = 0;
        for (WallblerItem item : wallblerItems) {
            if (!recentPostsIds.contains(item.getSocialId())) {
                payload.append("{\"index\":{\"_index\":\"")
                        .append(item.getSocialMediaType())
                        .append("\",\"_id\":\"")
                        .append(item.getSocialId())
                        .append("\"}}\n")
                        .append(new JSONObject(item))
                        .append("\n");
                count++;
            }
        }
        return new Payload(payload.toString(), count);
    }

    private Payload generateBulkPayloadForUpdating(Set<WallblerItem> wallblerItems, Set<Integer> recentPostsIds) {
        StringBuilder payload = new StringBuilder();
        int count = 0;
        for (WallblerItem item : wallblerItems) {
            if (recentPostsIds.contains(item.getSocialId())) {
                JSONObject obj = new JSONObject(item);
                obj.remove("accepted"); // in order to the 'accepted' field is not changing while updating
                payload.append("{\"update\":{\"_index\":\"")
                        .append(item.getSocialMediaType())
                        .append("\",\"_id\":\"")
                        .append(item.getSocialId())
                        .append("\"}}\n{\"doc\":")
                        .append(obj)
                        .append("}\n");
                count++;
            }
        }
        return new Payload(payload.toString(), count);
    }

    private Payload generateBulkPayloadForDeleting(String socialMediaType, Set<Integer> outdatedPostsIds) {
        StringBuilder payload = new StringBuilder();
        int count = 0;
        for (Integer postId : outdatedPostsIds) {
            payload.append("{\"delete\":{\"_index\":\"")
                    .append(socialMediaType)
                    .append("\",\"_id\":\"")
                    .append(postId)
                    .append("\"}}\n");
            count++;
        }
        return new Payload(payload.toString(), count);
    }

    private String generateBulkPayloadForAccepting(List<WallblerItem> wallblerItems) {
        StringBuilder payload = new StringBuilder();
        for (WallblerItem item : wallblerItems) {
            payload.append("{\"update\":{\"_index\":\"")
                    .append(item.getSocialMediaType())
                    .append("\",\"_id\":")
                    .append(item.getSocialId())
                    .append("}}\n{\"doc\":{\"accepted\":")
                    .append(item.isAccepted())
                    .append("}}\n");
        }
        return payload.toString();
    }

    static class ExistingPosts {
        private Set<JSONObject> recent = new HashSet<>();
        private Set<JSONObject> outdated = new HashSet<>();

        void add(JSONObject jsonObject) {
            if (recent.size() < WALLBLER_MAX_LIMIT) {
                recent.add(jsonObject);
            } else {
                outdated.add(jsonObject);
            }
        }

        Set<Integer> getRecentAsIds() {
            return recent.stream().map(a -> a.getInt("socialId")).collect(Collectors.toSet());
        }

        Set<Integer> getOutdatedAsIds() {
            return outdated.stream().map(a -> a.getInt("socialId")).collect(Collectors.toSet());
        }
    }

    static class Payload {
        private String payload;
        private int quantity;

        public Payload(String payload, int quantity) {
            this.payload = payload;
            this.quantity = quantity;
        }
    }

}
