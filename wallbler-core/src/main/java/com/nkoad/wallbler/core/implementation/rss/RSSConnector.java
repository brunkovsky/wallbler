package com.nkoad.wallbler.core.implementation.rss;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.core.Connector;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;
import java.util.*;

public class RSSConnector extends Connector {

    public RSSConnector(Map<String, Object> feedProperties, Dictionary<String, Object> accountProperties, Cache cache) {
        super(feedProperties, accountProperties, cache);
    }

    @Override
    public void loadData() {
        String url = (String) feedProperties.get("config.url");
        try {
            long lastRefreshDate = new Date().getTime();
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));
            Set<WallblerItem> wallblerItems = new HashSet<>();
            feed.getEntries().forEach(entity -> {
                WallblerItem item = new RSSWallblerItem(feedProperties);
                item.setLastRefreshDate(lastRefreshDate);
                item.setTitle(entity.getTitle());
                item.setDescription(entity.getDescription().getValue());
                item.setDate(entity.getPublishedDate().getTime());
                item.setLinkToSMPage(entity.getLink());
                item.setUrl(feed.getLink());
                wallblerItems.add(item);
            });
            cache.add(wallblerItems);
        } catch (Exception e) {
            LOGGER.error("can't retrieve rss data, feed url: " + url);
            e.printStackTrace();
        }
    }

}
