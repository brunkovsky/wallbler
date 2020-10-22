package com.nkoad.wallbler.core.definition.rss;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Wallbler RSS Account")
public @interface RSSAccountConfig {

    @AttributeDefinition(name = "Name", description = "Unique account name")
    String config_name()default "rss account";

    @AttributeDefinition(name = "Is enabled", description = "It's possible to temporary disable the account")
    boolean config_enabled() default true;

    @AttributeDefinition(name = "Is valid", description = "It indicates is the account valid. You should not set this checkbox. It will set automatically")
    boolean config_valid();

}
