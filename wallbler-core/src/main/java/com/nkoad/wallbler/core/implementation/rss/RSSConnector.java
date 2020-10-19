package com.nkoad.wallbler.core.implementation.rss;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.core.Connector;
import com.nkoad.wallbler.core.WallblerItems;
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
        int count = (int) feedProperties.get("config.count");
        try {
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));
            List<WallblerItem> wallblerItems = new ArrayList<>();
            feed.getEntries().stream().limit(count).forEach(entity -> {
                WallblerItem item = new RSSWallblerItem();
                item.setTitle(entity.getTitle());
                item.setDescription(entity.getDescription().getValue());
                item.setDate(entity.getPublishedDate().getTime());
                item.setLinkToSMPage(entity.getLink());
                item.setUrl(feed.getLink());
                item.setFeedName((String) feedProperties.get("config.name"));
                item.setFeedPid((String) feedProperties.get("service.pid"));
                item.setAccepted((boolean) feedProperties.get("config.acceptedByDefault"));
                item.generateSocialId();
                wallblerItems.add(item);
            });
            cache.add(new WallblerItems(wallblerItems));
        } catch (Exception e) {
            LOGGER.error("can't retrieve rss data, feed url: " + url);
            e.printStackTrace();
        }
    }

}
