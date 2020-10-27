package com.nkoad.wallbler.core.implementation.rss;

import com.nkoad.wallbler.core.definition.rss.RSSAccountConfig;
import com.nkoad.wallbler.core.Account;
import com.nkoad.wallbler.core.Validator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Map;

@Component
@Designate(ocd = RSSAccountConfig.class, factory = true)
public class RSSAccount extends Account<Validator> {

    @Override
    public void assignValidator(Map<String, Object> properties) {
        validator = new RSSValidator(properties);
    }

    @Activate
    public void activate(Map<String, Object> properties) {
        super.activate(properties);
        setValid(properties);
    }

    @Modified
    public void modified(Map<String, Object> properties) {
        super.modified(properties);
//        setValid(properties);
    }

    @Deactivate
    public void deactivate(Map<String, Object> properties) {
        super.deactivate(properties);
    }

}
