package com.nkoad.wallbler.core.definition.twitter;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Wallbler Twitter Feed")
public @interface TwitterFeedConfig {

    @AttributeDefinition(name = "Name", description = "Unique feed name")
    String config_name() default "twitter feed";

    @AttributeDefinition(name = "Account name", description = "Linked account name")
    String config_accountName() default "twitter account";

    @AttributeDefinition(name = "Is enabled", description = "It's possible to temporary disable the feed")
    boolean config_enabled() default true;

    @AttributeDefinition(name = "Apply filters", description = "Flag to enable/disable filters")
    boolean config_applyFilters();

    @AttributeDefinition(name = "Hashtag", description = "Finds Tweets containing hashtag(s)")
    String config_hashtag();

    @AttributeDefinition(name = "Media type", description = "Finds Tweets containing images/video/media")
    String config_mediaType();

    @AttributeDefinition(name = "Is safe", description = "Finds Tweets marked as potentially sensitive removed")
    boolean config_safe();

    @AttributeDefinition(name = "Retweets", description = "Finds Tweets filtering out retweets")
    boolean config_retweets();

    @AttributeDefinition(name = "Date from", description = "Finds Tweets sent since date")
    String config_dateFrom();

    @AttributeDefinition(name = "Date to", description = "Finds Tweets sent before date")
    String config_dateTo();

    @AttributeDefinition(name = "Account from", description = "Finds Tweets sent from account")
    String config_accountFrom();

    @AttributeDefinition(name = "Account to", description = "Finds Tweets a Tweet authored in reply to Twitter account")
    String config_accountTo();

    @AttributeDefinition(name = "Account mention", description = "Finds Tweets mentioning Twitter account")
    String config_accountMention();

    @AttributeDefinition(name = "Query", description = "Finds Tweets containing text")
    String config_query();

    @AttributeDefinition(name = "Delay", description = "Delay in hours", type = AttributeType.INTEGER)
    int config_delay() default 6;

    @AttributeDefinition(name = "Accepted by default")
    boolean config_acceptedByDefault() default true;

}
