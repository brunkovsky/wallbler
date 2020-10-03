package com.nkoad.wallbler.core.implementation.rss;

import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.core.WallblerItemPack;
import com.nkoad.wallbler.core.implementation.Connector;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RSSConnector extends Connector {

    public RSSConnector(Map<String, Object> feedProperties, Map<String, Object> accountProperties) {
        super(feedProperties, accountProperties);
    }

    @Override
    public WallblerItemPack getData() {
        String url = (String) feedProperties.get("config.url");
        int count = (int) feedProperties.get("config.count");
        try {
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));
            List<WallblerItem> wallblerItems = new ArrayList<>();
            feed.getEntries().stream().limit(count).forEach(entity -> {
                WallblerItem item = new RSSWallblerItem();
                item.setTitle(entity.getTitle());
                item.setDescription(entity.getDescription().getValue());
                item.setDate(entity.getPublishedDate());
                item.setLinkToSMPage(entity.getLink());
                item.setUrl(feed.getLink());
                item.generateSocialId();
                wallblerItems.add(item);
            });
            return new WallblerItemPack(wallblerItems);
        } catch (Exception e) {
            LOGGER.error("can't retrieve rss data, feed url: " + url);
        }
        return null;
    }

}
