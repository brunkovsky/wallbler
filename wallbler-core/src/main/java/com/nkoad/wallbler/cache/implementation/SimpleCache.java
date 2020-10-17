package com.nkoad.wallbler.cache.implementation;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.core.WallblerItemPack;
import com.nkoad.wallbler.httpConnector.GETConnector;
import com.nkoad.wallbler.httpConnector.HTTPConnector;
import com.nkoad.wallbler.httpConnector.POSTConnector;
import com.nkoad.wallbler.httpConnector.PUTConnector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.util.*;

@Component(name = "ElasticSearchCache", service = Cache.class)
public class SimpleCache implements Cache {

    @Override
    public void add(String feedPid, WallblerItemPack data) {
        System.out.println("---------------add");
        Map<Integer, Boolean> booleanMap = fetchManagedPosts(data);
        HTTPConnector httpConnector = new PUTConnector();
        try {
            for (WallblerItem datum : data.getData()) {
                if (!booleanMap.containsKey(datum.getSocialId())) {
                    String url = "http://localhost:9200/" + datum.getSocialMediaType() + "/_doc/" + datum.getSocialId();
                    JSONObject jsonObject = new JSONObject(datum);
                    jsonObject.put("feedPid", feedPid);
                    jsonObject.put("lastRefreshDate", data.getLastRefreshDate().getTime());
                    httpConnector.httpRequest(url, jsonObject.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JSONArray getData(String socials, Boolean accepted) {
        System.out.println("---------------getData");
        JSONArray result = new JSONArray();
        try {
            for (String social : socials.split(",")) {
                String url = "http://localhost:9200/" + social + "/_search";
                HTTPRequest httpRequest = new GETConnector().httpRequest(url);
                JSONArray jsonArray = new JSONObject(httpRequest.getBody()).getJSONObject("hits").getJSONArray("hits");
                for (int i = 0; i < jsonArray.length(); i++) {
                    result.put(jsonArray.getJSONObject(i).getJSONObject("_source"));
                }
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
        try {
            for (WallblerItem wallblerItem : wallblerItems) {
                String url = "http://localhost:9200/" + wallblerItem.getSocialMediaType() + "/_doc/" + wallblerItem.getSocialId() + "/_update";
                httpConnector.httpRequest(url, "{\"doc\":{\"accepted\":" + wallblerItem.isAccepted() + "}}");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeFromCache(String feedPid) {
        System.out.println("---------------removeFromCache");
        try {
            String socialMediaType = extractSocialMediaType(feedPid);
            String url = "http://localhost:9200/" + socialMediaType + "/_delete_by_query";
            new POSTConnector().httpRequest(url, "{\"query\":{\"match\":{\"feedPid\":\"" + feedPid + "\"}}}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractSocialMediaType(String feedPid) {
        return feedPid.split("\\.")[5];
    }

    private Map<Integer, Boolean> fetchManagedPosts(WallblerItemPack data) {
        Map<Integer, Boolean> result = new HashMap<>();
        String socialMediaType = data.getData().stream().findFirst().get().getSocialMediaType();
        JSONArray getData = getData(socialMediaType, null);
        for (int i = 0; i < getData.length(); i++) {
            JSONObject jsonObject = getData.getJSONObject(i);
            int socialId = jsonObject.getInt("socialId");
            if (!jsonObject.isNull("accepted")) {
                boolean accepted = jsonObject.getBoolean("accepted");
                result.put(socialId, accepted);
            }
        }
        return result;
    }

}
