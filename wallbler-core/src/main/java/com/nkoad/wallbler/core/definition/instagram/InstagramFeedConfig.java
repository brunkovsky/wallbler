package com.nkoad.wallbler.core.definition.instagram;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Wallbler Instagram Feed")
public @interface InstagramFeedConfig {

    @AttributeDefinition(name = "Name", description = "Unique feed name")
    String config_name() default "instagram feed";

    @AttributeDefinition(name = "Account name", description = "Linked account name")
    String config_accountName() default "instagram account";

    @AttributeDefinition(name = "Is enabled", description = "It's possible to temporary disable the feed")
    boolean config_enabled() default true;

    @AttributeDefinition(name = "Delay", description = "Delay in hours")
    int config_delay() default 6;

    @AttributeDefinition(name = "Accepted by default")
    boolean config_acceptedByDefault() default true;

}
