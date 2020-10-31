package com.nkoad.wallbler.core.definition.facebook;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(name = "Wallbler Facebook Feed")
public @interface FacebookFeedConfig {

    @AttributeDefinition(name = "Name", description = "Unique feed name")
    String config_name() default "facebook feed";

    @AttributeDefinition(name = "Account name", description = "Linked account name")
    String config_accountName() default "facebook account";

    @AttributeDefinition(name = "Type of feed", options = { @Option(label = "posts", value = "posts"), @Option(label = "photos", value = "photos"), @Option(label = "videos", value = "videos"), @Option(label = "albums", value = "albums")})
    String config_typeOfFeed() default "posts";

    @AttributeDefinition(name = "Album", description = "Album's id")
    String config_album();

    @AttributeDefinition(name = "Is enabled", description = "It's possible to temporary disable the feed")
    boolean config_enabled() default true;

    @AttributeDefinition(name = "Delay", description = "Delay in hours")
    int config_delay() default 6;

    @AttributeDefinition(name = "Accepted by default")
    boolean config_acceptedByDefault() default true;

}
