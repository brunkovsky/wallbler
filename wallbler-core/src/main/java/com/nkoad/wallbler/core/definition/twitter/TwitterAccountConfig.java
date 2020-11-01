package com.nkoad.wallbler.core.definition.twitter;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Wallbler Twitter Account")
public @interface TwitterAccountConfig {

    @AttributeDefinition(name = "Name", description = "Unique account name")
    String config_name() default "twitter account";

    @AttributeDefinition(name = "Is enabled", description = "It's possible to temporary disable the account")
    boolean config_enabled() default true;

    @AttributeDefinition(name = "Is valid", description = "It indicates is the account valid. You should not set this checkbox. It will set automatically")
    boolean config_valid();

    @AttributeDefinition(name = "oAuthConsumerKey")
    String config_oAuthConsumerKey() default "Z0TqmgWEc2u63lp11XsFu8FxG";

    @AttributeDefinition(name = "oAuthConsumerSecret")
    String config_oAuthConsumerSecret() default "9gUQaXmdlkUXs8YsJBB6jvHrx8nOjsPrc9PW1Os8HStzk5lNSO";

    @AttributeDefinition(name = "oAuthAccessToken")
    String config_oAuthAccessToken() default "881847444128059393-slna9X1P4BckmDIDnVfuThJumqSy3Q9";

    @AttributeDefinition(name = "oAuthAccessTokenSecret")
    String config_oAuthAccessTokenSecret() default "5gnyAetIZtuCIPixGUWaz1FpRGVP1HUnphNvc1FAs8OeE";

}
