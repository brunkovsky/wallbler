package com.nkoad.wallbler.core.definition.instagram;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Wallbler Instagram Account")
public @interface InstagramAccountConfig {

    @AttributeDefinition(name = "Name", description = "Unique account name")
    String config_name() default "instagram account";

    @AttributeDefinition(name = "Is enabled", description = "It's possible to temporary disable the account")
    boolean config_enabled() default true;

    @AttributeDefinition(name = "Is valid", description = "It indicates is the account valid. You should not set this checkbox. It will set automatically")
    boolean config_valid();

    @AttributeDefinition(name = "Access token")
    String config_accessToken() default "IGQVJVOHN4SjJISms0WWdGcmN2UUlsUER5VlEwRmJXbHJjOXhBclJvaU5MdzBsNWE3eFRNUDBRYk9oeVphamtMbDcwMXhGOHF0OFg2OXNadldOZAFZAJVHVJRDN3OEpDUWNyelZA6eWpn";

    @AttributeDefinition(name = "Refresh period. In days", description = "Refresh access token. No refreshing if '0'")
    int config_refresh() default 5;

}
