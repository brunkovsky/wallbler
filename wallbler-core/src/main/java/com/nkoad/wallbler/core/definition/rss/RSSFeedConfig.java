package com.nkoad.wallbler.core.definition.rss;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(name = "Wallbler RSS Feed")
public @interface RSSFeedConfig {

    @AttributeDefinition(name = "Name", description = "Unique feed name")
    String config_name() default "rss feed";

    @AttributeDefinition(name = "Account name", description = "Linked account name")
    String config_accountName() default "rss account";

    @AttributeDefinition(name = "RSS url", description = "RSS url")
    String config_url() default "https://www.nasa.gov/rss/dyn/breaking_news.rss";

    @AttributeDefinition(name = "Is enabled", description = "It's possible to temporary disable the feed")
    boolean config_enabled() default true;

    @AttributeDefinition(name = "Delay", description = "Delay in hours")
    int config_delay() default 6;

    @AttributeDefinition(name = "Accepted by default")
    boolean config_acceptedByDefault() default true;

}
