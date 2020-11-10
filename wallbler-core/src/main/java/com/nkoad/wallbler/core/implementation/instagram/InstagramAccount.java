package com.nkoad.wallbler.core.implementation.instagram;

import com.nkoad.wallbler.core.definition.instagram.InstagramAccountConfig;
import com.nkoad.wallbler.core.RefreshableAccount;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Map;

@Component
@Designate(ocd = InstagramAccountConfig.class, factory = true)
public class InstagramAccount extends RefreshableAccount {

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
        super.activate(properties);
        setValid(properties);
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
