package com.nkoad.wallbler.core.implementation.rss;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.definition.rss.RSSFeedConfig;
import com.nkoad.wallbler.core.Feed;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Map;

@Component
@Designate(ocd = RSSFeedConfig.class, factory = true)
public class RSSFeed extends Feed {
    @Reference
    private Cache cache;

    @Override
    public void assignConnector(Map<String, Object> properties) {
        connector = new RSSConnector(properties, extractAccountProperties(properties), cache);
    }

    @Activate
    public void activate(Map<String, Object> properties) {
        super.activate(properties);
    }

    @Modified
    public void modified(Map<String, Object> properties) {
        super.modified(properties);
    }

    @Deactivate
    public void deactivate(Map<String, Object> properties) {
        super.deactivate(properties);
    }

}
