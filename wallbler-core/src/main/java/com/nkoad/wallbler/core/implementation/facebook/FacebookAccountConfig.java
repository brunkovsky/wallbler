package com.nkoad.wallbler.core.implementation.facebook;

import com.nkoad.wallbler.core.OSGIConfig;
import com.nkoad.wallbler.core.implementation.AccountConfig;
import com.nkoad.wallbler.core.implementation.Validator;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Map;

@Component
@Designate(ocd = com.nkoad.wallbler.core.definition.facebook.FacebookAccountConfig.class, factory = true)
public class FacebookAccountConfig extends AccountConfig<Validator> {
    @Reference
    private OSGIConfig refOsgiConfig;

    @Override
    public void assignValidator(Map<String, Object> properties) {
        validator = new FacebookValidator(properties);
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
