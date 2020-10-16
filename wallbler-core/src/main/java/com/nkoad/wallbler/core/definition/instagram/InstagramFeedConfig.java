package com.nkoad.wallbler.core.definition.instagram;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Wallbler Instagram Feed")
public @interface InstagramFeedConfig {

    @AttributeDefinition(name = "Name", description = "Unique Feed name")
    String config_name() default "instagram feed";

    @AttributeDefinition(name = "Account Name", description = "Linked Account Name")
    String config_accountName() default "instagram account";

    @AttributeDefinition(name = "Is enabled", description = "It's possible to temporary disable the feed")
    boolean config_enabled() default true;

    @AttributeDefinition(name = "Count", description = "Quantity", type = AttributeType.INTEGER)
    int config_count() default 10;

    @AttributeDefinition(name = "Delay", description = "Delay in seconds", type = AttributeType.INTEGER)
    int config_delay() default 10;

//    @AttributeDefinition(name = "Accepted by default")
//    boolean config_acceptedByDefault() default true;

}
