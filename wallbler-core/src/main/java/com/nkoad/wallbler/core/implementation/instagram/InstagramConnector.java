package com.nkoad.wallbler.core.implementation.instagram;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.HTTPConnector;
import com.nkoad.wallbler.core.HTTPRequest;
import com.nkoad.wallbler.core.Connector;

import java.util.Dictionary;
import java.util.Map;

public class InstagramConnector extends Connector {

    public InstagramConnector(Map<String, Object> feedProperties, Dictionary<String, Object> accountProperties, Cache cache) {
        super(feedProperties, accountProperties, cache);
    }

    @Override
    public void getData() {
        try {
            String url = (String) feedProperties.get("config.url");
            int count = (int) feedProperties.get("config.count");
            HTTPRequest httpRequest = new HTTPConnector().httpGetRequest(url);
            if (httpRequest.getStatusCode() == 200) {
                LOGGER.info("Instagram 200");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
