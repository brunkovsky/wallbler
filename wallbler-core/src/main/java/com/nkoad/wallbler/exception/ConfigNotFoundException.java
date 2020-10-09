package com.nkoad.wallbler.exception;

public class ConfigNotFoundException extends IllegalArgumentException {

    public ConfigNotFoundException(String pid) {
        super(String.format("can not find config. pid: %s", pid));
    }

}
