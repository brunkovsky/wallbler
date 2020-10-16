package com.nkoad.wallbler.core.definition.facebook;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Wallbler Facebook Account")
public @interface FacebookAccountConfig {

    @AttributeDefinition(name = "Name", description = "Unique account name")
    String config_name() default "facebook account";

    @AttributeDefinition(name = "Is enabled", description = "It's possible to temporary disable the account")
    boolean config_enabled() default true;

    @AttributeDefinition(name = "Is valid", description = "It indicates is the account valid. You should not set this checkbox. It will set automatically")
    boolean config_valid() default false;

    @AttributeDefinition(name = "Group Key")
    String config_groupId() default "663377994414452";

    @AttributeDefinition(name = "oAuthAccessToken")
    String config_oAuthAccessToken() default "206081670615853|e26e9591d8b92d0e5262871b09eb49d1";

}
