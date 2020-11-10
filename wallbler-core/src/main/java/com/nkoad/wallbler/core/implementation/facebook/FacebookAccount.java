package com.nkoad.wallbler.core.implementation.facebook;

import com.nkoad.wallbler.core.definition.facebook.FacebookAccountConfig;
import com.nkoad.wallbler.core.Account;
import com.nkoad.wallbler.core.Validator;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Map;

@Component
@Designate(ocd = FacebookAccountConfig.class, factory = true)
public class FacebookAccount extends Account<Validator> {

    @Override
    public void assignValidator(Map<String, Object> properties) {
        validator = new FacebookValidator(properties);
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
