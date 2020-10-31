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
    private final static String FILTER_BY_FEED_NAME_PAYLOAD_TEMPLATE = "\"query\":{\"match\":{\"feedName\":\"%s\"}}";
    private final static int MAX_LIMIT = 10000;  // max limit for '_search' in elasticsearch by default
    private final static int WALLBLER_MAX_LIMIT = 200;  // define max limit for each social type in the cache

    @Override
    public void add(Set<WallblerItem> wallblerItems) {
        WallblerItem wallblerItem = wallblerItems.stream().findAny().get();
        LOGGER.info("putting data to the cache. social: "
                + wallblerItem.getSocialMediaType()
                + ". feed name: "
                + wallblerItem.getFeedName()
                + ". size: " + wallblerItems.size());
        ExistedPosts existedPostsId = fetchExistedPosts(wallblerItem.getSocialMediaType());
        String payloadForAdding = generateBulkPayloadForAdding(wallblerItems, existedPostsId.getRecent());
        if (!payloadForAdding.isEmpty()) {
            try {
                new POSTConnectorNdjsonContentType().httpRequest(BULK_URL, payloadForAdding);
            } catch (IOException e) {
                LOGGER.error("can not add posts");
                e.printStackTrace();
            }
        }
        String payloadForUpdating = generateBulkPayloadForUpdating(wallblerItems, existedPostsId.getRecent());
        if (!payloadForUpdating.isEmpty()) {
            try {
                new POSTConnectorNdjsonContentType().httpRequest(BULK_URL, payloadForUpdating);
            } catch (IOException e) {
                LOGGER.error("can not update posts");
                e.printStackTrace();
            }
        }
        Set<Integer> outdatedPosts = existedPostsId.getOutdated();
        if (!outdatedPosts.isEmpty()) {
            deleteOutdatedPosts(wallblerItem.getSocialMediaType(), outdatedPosts);
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

    private ExistedPosts fetchExistedPosts(String socialMediaType) {
        JSONArray existedPosts = getData(socialMediaType, MAX_LIMIT, "{"
                + SORT_BY_DATE_DESC_PAYLOAD
                + "}");
        ExistedPosts result = new ExistedPosts();
        for (int i = 0; i < existedPosts.length(); i++) {
            JSONObject jsonObject = existedPosts.getJSONObject(i);
            result.add(jsonObject.getInt("socialId"));
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

    private String generateBulkPayloadForUpdating(Set<WallblerItem> wallblerItems, Set<Integer> existedPostsId) {
        StringBuilder payload = new StringBuilder();
        for (WallblerItem wallblerItem : wallblerItems) {
            if (existedPostsId.contains(wallblerItem.getSocialId())) {
                JSONObject obj = new JSONObject(wallblerItem);
                obj.remove("accepted");
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

    static class ExistedPosts {
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
