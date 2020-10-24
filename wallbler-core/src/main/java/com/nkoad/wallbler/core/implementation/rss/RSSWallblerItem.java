package com.nkoad.wallbler.core.implementation.rss;

import com.nkoad.wallbler.core.WallblerItem;

import java.util.Map;

public class RSSWallblerItem extends WallblerItem {

    public RSSWallblerItem(Map<String, Object> feedProperties) {
        super(feedProperties);
    }

    @Override
    public String toString() {
        return "{" +
                "\"lastRefreshDate\":" + lastRefreshDate +
                ",\"socialId\":" + socialId +
                ",\"socialMediaType\":\"" + socialMediaType + "\"" +
                ",\"feedName\":\"" + feedName + "\"" +
                ",\"title\":\"" + title + "\"" +
                ",\"description\":\"" + description + "\"" +
                ",\"date\":" + date +
                ",\"url\":\"" + url + "\"" +
                ",\"linkToSMPage\":\"" + linkToSMPage + "\"" +
                ",\"accepted\":" + accepted +
                "}";
    }

}
