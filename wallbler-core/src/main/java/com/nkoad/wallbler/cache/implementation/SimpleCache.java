package com.nkoad.wallbler.cache.implementation;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.httpConnector.GETConnector;
import com.nkoad.wallbler.httpConnector.HTTPConnector;
import com.nkoad.wallbler.httpConnector.POSTConnector;
import com.nkoad.wallbler.httpConnector.PUTConnector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Component(name = "ElasticSearchCache", service = Cache.class)
public class SimpleCache implements Cache {
    private static String HOST = "http://localhost:9200/";
    private static String ADD_URL = HOST + "%s/_doc/%s";
    private static String SEARCH_URL = HOST + "%s/_search";
    private static String UPDATE_URL = HOST + "%s/_doc/%s/_update";
    private static String REMOVE_URL = HOST + "%s/_delete_by_query";
    private static String ACCEPT_PAYLOAD = "{\"doc\":{\"accepted\":%s}}";
    private static String REMOVE_BY_FEED_PID_PAYLOAD = "{\"query\":{\"match\":{\"feedPid\":\"%s\"}}}";

    @Override
    public void add(Set<WallblerItem> data) {
        System.out.println("---------------add");
        HTTPConnector httpConnector = new PUTConnector();
        data.stream()
                .filter(a -> !fetchManagedPosts(data).contains(a.getSocialId()))
                .forEach(a -> {
                    String url = String.format(ADD_URL, a.getSocialMediaType(), a.getSocialId());
                    String payload = new JSONObject(a).toString();
                    try {
                        httpConnector.httpRequest(url, payload);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public JSONArray getData(String socials) {
        System.out.println("---------------getData");
        JSONArray result = new JSONArray();
        try {
            GETConnector getConnector = new GETConnector();
            for (String social : socials.split(",")) {
                String url = String.format(SEARCH_URL, social);
                try {
                    HTTPRequest httpRequest = getConnector.httpRequest(url);
                    JSONArray jsonArray = new JSONObject(httpRequest.getBody()).getJSONObject("hits").getJSONArray("hits");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        result.put(jsonArray.getJSONObject(i).getJSONObject("_source"));
                    }
                } catch (FileNotFoundException ignore) {}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void setAccept(List<WallblerItem> wallblerItems) {
        System.out.println("---------------setAccept");
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
    public void removeFromCache(String feedPid) {
        System.out.println("---------------removeFromCache");
        try {
            String url = String.format(REMOVE_URL, extractSocialMediaType(feedPid));
            String payload = String.format(REMOVE_BY_FEED_PID_PAYLOAD, feedPid);
            new POSTConnector().httpRequest(url, payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<Integer> fetchManagedPosts(Set<WallblerItem> data) {
        WallblerItem wallblerItem = data.stream().findFirst().get();
        String socialMediaType = wallblerItem.getSocialMediaType();
        JSONArray getData = getData(socialMediaType);
        Set<Integer> result = new HashSet<>();
        for (int i = 0; i < getData.length(); i++) {
            JSONObject jsonObject = getData.getJSONObject(i);
            int socialId = jsonObject.getInt("socialId");
            if (!jsonObject.isNull("accepted")) {
                result.add(socialId);
            }
        }
        return result;
    }

    private String extractSocialMediaType(String feedPid) {
        return feedPid.split("\\.")[5];
    }

}
