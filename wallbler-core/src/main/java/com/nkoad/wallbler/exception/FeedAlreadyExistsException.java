package com.nkoad.wallbler.exception;

public class FeedAlreadyExistsException extends IllegalArgumentException {

    public FeedAlreadyExistsException(String pid, String name) {
        super(String.format("feed already exists. pid: %s, name: %s", pid, name));
    }

}
