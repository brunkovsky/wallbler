package com.nkoad.wallbler.core.definition.rss;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(name = "Wallbler RSS Feed")
public @interface RSSFeedConfig {

    @AttributeDefinition(name = "Name", description = "Unique Feed name")
    String config_name() default "rss feed";

    @AttributeDefinition(name = "Account Name", description = "Linked Account Name")
    String config_accountName() default "rss account";

    @AttributeDefinition(name = "RSS url", description = "RSS url")
    String config_url();

    @AttributeDefinition(name = "Is enabled", description = "It's possible to temporary disable the account")
    boolean config_enabled() default true;

    @AttributeDefinition(name = "Count", description = "Quantity. 10 is maximum", type = AttributeType.INTEGER)
    int config_count() default 1;

    @AttributeDefinition(name = "Delay", description = "Delay in seconds", type = AttributeType.INTEGER)
    int config_delay() default 100;

    @AttributeDefinition(name = "Accepted by default")
    boolean config_acceptedByDefault() default true;

}
