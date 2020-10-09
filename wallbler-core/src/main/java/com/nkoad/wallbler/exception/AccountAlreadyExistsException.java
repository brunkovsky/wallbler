package com.nkoad.wallbler.exception;

public class AccountAlreadyExistsException extends IllegalArgumentException {

    public AccountAlreadyExistsException(String pid, String name) {
        super(String.format("account already exists. pid: %s, name: %s", pid, name));
    }

}
