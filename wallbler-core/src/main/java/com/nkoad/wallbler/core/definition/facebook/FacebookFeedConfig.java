package com.nkoad.wallbler.core.definition.facebook;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
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

    @AttributeDefinition(name = "Is enabled", description = "It's possible to temporary disable the feed")
    boolean config_enabled() default true;

    @AttributeDefinition(name = "Count", description = "Quantity", type = AttributeType.INTEGER)
    int config_count() default 1;

    @AttributeDefinition(name = "Delay", description = "Delay in seconds", type = AttributeType.INTEGER)
    int config_delay() default 100;

    @AttributeDefinition(name = "Accepted by default")
    boolean config_acceptedByDefault() default true;

}
