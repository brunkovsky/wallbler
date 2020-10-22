package com.nkoad.wallbler.core.definition.instagram;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Icon;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Wallbler Instagram Account", icon = @Icon(resource = "icon/instagram-32.png", size = 32))
public @interface InstagramAccountConfig {

    @AttributeDefinition(defaultValue = "instagram account", name = "Name", description = "Unique account name")
    String config_name() default "instagram account";

    @AttributeDefinition(name = "Is enabled", description = "It's possible to temporary disable the account")
    boolean config_enabled() default true;

    @AttributeDefinition(name = "Is valid", description = "It indicates is the account valid. You should not set this checkbox. It will set automatically")
    boolean config_valid();

    @AttributeDefinition(name = "Access token")
    String config_accessToken();

    @AttributeDefinition(name = "Refresh", description = "Refresh access token", type = AttributeType.INTEGER, min = "0", required = false)
    int config_refresh() default 100;

}
