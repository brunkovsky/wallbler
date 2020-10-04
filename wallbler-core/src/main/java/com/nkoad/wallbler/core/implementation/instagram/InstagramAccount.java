package com.nkoad.wallbler.core.implementation.instagram;

import com.nkoad.wallbler.core.OSGIConfig;
import com.nkoad.wallbler.core.definition.instagram.InstagramAccountConfig;
import com.nkoad.wallbler.core.implementation.RefreshableAccount;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Map;

@Component
@Designate(ocd = InstagramAccountConfig.class, factory = true)
public class InstagramAccount extends RefreshableAccount {
    @Reference
    private OSGIConfig refOsgiConfig;

    @Override
    public void assignValidator(Map<String, Object> properties) {
        validator = new InstagramValidator(properties);
    }

    @Override
    protected String refreshAccessToken() {
        return validator.refreshAccessToken();
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
