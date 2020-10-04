package com.nkoad.wallbler.core.implementation.rss;

import com.nkoad.wallbler.core.OSGIConfig;
import com.nkoad.wallbler.core.definition.rss.RSSAccountConfig;
import com.nkoad.wallbler.core.implementation.Account;
import com.nkoad.wallbler.core.implementation.Validator;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Map;

@Component
@Designate(ocd = RSSAccountConfig.class, factory = true)
public class RSSAccount extends Account<Validator> {
    @Reference
    private OSGIConfig refOsgiConfig;

    @Override
    public void assignValidator(Map<String, Object> properties) {
        validator = new RSSValidator(properties);
    }

    @Activate
    public void activate(Map<String, Object> properties) {
        super.setOsgiConfig(refOsgiConfig);
        super.activate(properties);
        setValid(properties);
    }

    @Modified
    public void modified(Map<String, Object> properties) {
        super.setOsgiConfig(refOsgiConfig);
        super.modified(properties);
        setValid(properties);
    }

    @Deactivate
    public void deactivate(Map<String, Object> properties) {
        super.deactivate(properties);
    }

}
