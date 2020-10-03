package com.nkoad.wallbler.core.implementation.instagram;

import com.nkoad.wallbler.core.OSGIConfig;
import com.nkoad.wallbler.core.implementation.FeedConfig;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Map;

@Component
@Designate(ocd = com.nkoad.wallbler.core.definition.instagram.InstagramFeedConfig.class, factory = true)
public class InstagramFeedConfig extends FeedConfig {
    @Reference
    private OSGIConfig osgiConfig;

    @Override
    public void assignConnector(Map<String, Object> properties) {
        connector = new InstagramConnector(properties, osgiConfig.extractAccountProperties(properties));
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
