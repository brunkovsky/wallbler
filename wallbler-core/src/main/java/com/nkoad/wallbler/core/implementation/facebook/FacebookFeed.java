package com.nkoad.wallbler.core.implementation.facebook;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.definition.facebook.FacebookFeedConfig;
import com.nkoad.wallbler.core.Feed;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Dictionary;
import java.util.Map;

@Component
@Designate(ocd = FacebookFeedConfig.class, factory = true)
public class FacebookFeed extends Feed {
    @Reference
    private Cache cache;

    @Override
    public void assignConnector(Map<String, Object> feedProperties, Map<String, Object> accountProperties) {
        connector = new FacebookConnector(feedProperties, accountProperties, cache);
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
