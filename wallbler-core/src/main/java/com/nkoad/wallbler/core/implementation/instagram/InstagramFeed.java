package com.nkoad.wallbler.core.implementation.instagram;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.OSGIConfig;
import com.nkoad.wallbler.core.definition.instagram.InstagramFeedConfig;
import com.nkoad.wallbler.core.implementation.Feed;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Map;

@Component
@Designate(ocd = InstagramFeedConfig.class, factory = true)
public class InstagramFeed extends Feed {
    @Reference
    private OSGIConfig osgiConfig;
    @Reference
    private Cache cache;

    @Override
    public void assignConnector(Map<String, Object> properties) {
        connector = new InstagramConnector(properties, osgiConfig.extractAccountProperties(properties), cache);
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
