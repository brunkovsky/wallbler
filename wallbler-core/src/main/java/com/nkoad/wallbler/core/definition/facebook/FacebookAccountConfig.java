package com.nkoad.wallbler.core.definition.facebook;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Wallbler Facebook Account")
public @interface FacebookAccountConfig {

    @AttributeDefinition(name = "Name", description = "Unique account name")
    String config_name() default "facebook account";

    @AttributeDefinition(name = "Is enabled", description = "It's possible to temporary disable the account")
    boolean config_enabled() default true;

    @AttributeDefinition(name = "Is valid", description = "It indicates is the account valid. You should not set this checkbox. It will set automatically")
    boolean config_valid();

    @AttributeDefinition(name = "Group Key")
    String config_groupId() default "111349400493820";

    @AttributeDefinition(name = "oAuthAccessToken")
    String config_oAuthAccessToken() default "EAApiYthtIG8BAA9tsFS3zXZCiX9fixZCCbr9wyrJujZBkbrIq8AZAI3VCzMVlSvdhoMemsuFnNNrFG7isCmVZBEI4nipZBKY11XaeJTYl9ReT7kiKxRzmpZBqTZBZASH5aaJTGu01tjjXMhQ9oG1PY2xqRfXUZCTudTFRSaA7o5Pk2wQZDZD";

}
