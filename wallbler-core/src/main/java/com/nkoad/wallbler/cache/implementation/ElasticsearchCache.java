package com.nkoad.wallbler.cache.implementation;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.httpConnector.GETConnector;
import com.nkoad.wallbler.httpConnector.POSTConnector;
import com.nkoad.wallbler.httpConnector.POSTConnectorNdjsonContentType;
import com.nkoad.wallbler.httpConnector.PUTConnector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component(name = "ElasticsearchCache", service = Cache.class)
public class ElasticsearchCache implements Cache {
    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchCache.class);
    private final static String HOST = "http://localhost:9200/";
    private final static String BULK_URL = HOST + "_bulk";
    private final static String SEARCH_URL_TEMPLATE = HOST + "%s/_search?size=%d";
    private final static String DELETE_URL_TEMPLATE = HOST + "%s/_delete_by_query";
    private final static String SORT_BY_DATE_DESC_PAYLOAD = "\"sort\":[{\"date\":{\"order\":\"desc\"}}]";
    private final static String FILTER_BY_ACCEPTED_FALSE_PAYLOAD = "\"query\":{\"match\":{\"accepted\":false}}";
    private final static String FILTER_BY_ACCEPTED_TRUE_PAYLOAD = "\"query\":{\"match\":{\"accepted\":true}}";
    private final static String FILTER_BY_FEED_NAME_PAYLOAD_TEMPLATE = "\"query\":{\"match_phrase\":{\"feedName\":\"%s\"}}";
    private final static int MAX_LIMIT = 10000;  // max limit for '_search' in elasticsearch by default
    private final static int SOCIAL_TYPE_MAX_LIMIT = 1000;  // define max limit for each social type in the cache

    // TODO : need to discuss how to implement it in other extensions such as 'youtube' and 'rss' (Activator?)
    @Activate
    void activate() {
//        createIndex("facebook");
//        createIndex("instagram");
//        createIndex("twitter");
    }

    @Override
    public void add(Set<WallblerItem> wallblerItemsToHandle) {
        WallblerItem wallblerItem = wallblerItemsToHandle.stream().findAny().get();
        String socialMediaTypeToHandle = wallblerItem.getSocialMediaType();
        String feedNameToHandle = wallblerItem.getFeedName();
        ExistingPosts existingPosts = retrieveExistingPosts(socialMediaTypeToHandle);
        LOGGER.info("refreshing cache for social: '" + socialMediaTypeToHandle + "'"
                + ". feed name: '" + feedNameToHandle + "'"
                + ". recent posts quantity: " + existingPosts.recent.size()
                + ". outdated posts quantity: " + existingPosts.outdated.size());

        Set<WallblerItem> itemsForAdding = existingPosts.onlyItemsForAdding(wallblerItemsToHandle);
        if (itemsForAdding.size() > 0) {
            String payloadForAdding = generateBulkPayloadForAdding(itemsForAdding);
            LOGGER.info("adding new posts for social: '" + socialMediaTypeToHandle + "'"
                    + ". feed name: '" + feedNameToHandle + "'"
                    + ". quantity to add: " + itemsForAdding.size());
            try {
                new POSTConnectorNdjsonContentType().httpRequest(BULK_URL, payloadForAdding);
            } catch (IOException e) {
                LOGGER.error("can not add posts");
                e.printStackTrace();
            }
        }

        Set<WallblerItem> itemsForUpdating = existingPosts.onlyItemsForUpdating(wallblerItemsToHandle);
        if (itemsForUpdating.size() > 0) {
            String payloadForUpdating = generateBulkPayloadForUpdating(itemsForUpdating);
            LOGGER.info("updating recent posts for social: '" + socialMediaTypeToHandle + "'"
                    + ". feed name: '" + feedNameToHandle + "'"
                    + ". quantity to update: " + itemsForUpdating.size());
            try {
                new POSTConnectorNdjsonContentType().httpRequest(BULK_URL, payloadForUpdating);
            } catch (IOException e) {
                LOGGER.error("can not update posts");
                e.printStackTrace();
            }
        }

        // TODO : I don't like how deleting proceed. better to delete only feedNameToHandle's posts -> need to filter it
        if (existingPosts.outdatedAsIds.size() > 0) {
            String payloadForDeleting = generateBulkPayloadForDeleting(socialMediaTypeToHandle, existingPosts.outdatedAsIds);
            LOGGER.info("deleting outdated posts for social: '" + socialMediaTypeToHandle + "'"
                    + ". feed name: '" + feedNameToHandle + "'"
                    + ". quantity to delete: " + existingPosts.outdatedAsIds.size());
            try {
                Thread.sleep(3000); // TODO: try to remove it...
                new POSTConnectorNdjsonContentType().httpRequest(BULK_URL, payloadForDeleting);
            } catch (Exception e) { // TODO: ...then 'IOException' here
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
                LOGGER.error("can not set 'accept' to elasticsearch");
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

    private void createIndex(String index) {
        LOGGER.debug("creating index for social: " + index);
        try {
            new PUTConnector().httpRequest(HOST + index); // TODO : maybe it needs to get the response and analyze it
        } catch (IOException e) {
            LOGGER.error("can not create index for social: " + index);
            e.printStackTrace();
        }
    }

    private JSONArray getData(String socials, Integer limit, String payload) {
        LOGGER.debug("getting posts from cache. socials: " + socials + ". limit: " + limit);
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
            LOGGER.error("can not get posts from elasticsearch");
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

    private String generateBulkPayloadForAdding(Set<WallblerItem> wallblerItems) {
        StringBuilder payload = new StringBuilder();
        for (WallblerItem item : wallblerItems) {
            payload.append("{\"index\":{\"_index\":\"")
                    .append(item.getSocialMediaType())
                    .append("\",\"_id\":\"")
                    .append(item.getSocialId())
                    .append("\"}}\n")
                    .append(new JSONObject(item))
                    .append("\n");
        }
        return payload.toString();
    }

    private String generateBulkPayloadForUpdating(Set<WallblerItem> wallblerItems) {
        StringBuilder payload = new StringBuilder();
        for (WallblerItem item : wallblerItems) {
            JSONObject obj = new JSONObject(item);
            obj.remove("accepted"); // in order to the 'accepted' field is not changing while updating
            payload.append("{\"update\":{\"_index\":\"")
                    .append(item.getSocialMediaType())
                    .append("\",\"_id\":\"")
                    .append(item.getSocialId())
                    .append("\"}}\n{\"doc\":")
                    .append(obj)
                    .append("}\n");
        }
        return payload.toString();
    }

    private String generateBulkPayloadForDeleting(String socialMediaType, Set<Integer> outdatedPostsIds) {
        StringBuilder payload = new StringBuilder();
        for (Integer postId : outdatedPostsIds) {
            payload.append("{\"delete\":{\"_index\":\"")
                    .append(socialMediaType)
                    .append("\",\"_id\":\"")
                    .append(postId)
                    .append("\"}}\n");
        }
        return payload.toString();
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
        private Set<Integer> recentAsIds = new HashSet<>();
        private Set<Integer> outdatedAsIds = new HashSet<>();

        void add(JSONObject jsonObject) {
            if (recent.size() < SOCIAL_TYPE_MAX_LIMIT) {
                recent.add(jsonObject);
                recentAsIds.add(jsonObject.getInt("socialId"));
            } else {
                outdated.add(jsonObject);
                outdatedAsIds.add(jsonObject.getInt("socialId"));
            }
        }

        Set<WallblerItem> onlyItemsForAdding(Set<WallblerItem> wallblerItems) {
            return wallblerItems.stream()
                    .filter(a -> !recentAsIds.contains(a.getSocialId()))
                    .collect(Collectors.toSet());
        }

        Set<WallblerItem> onlyItemsForUpdating(Set<WallblerItem> wallblerItems) {
            return wallblerItems.stream()
                    .filter(a -> recentAsIds.contains(a.getSocialId()))
                    .collect(Collectors.toSet());
        }
    }

}
