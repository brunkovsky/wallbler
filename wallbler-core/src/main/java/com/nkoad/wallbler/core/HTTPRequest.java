package com.nkoad.wallbler.core;

public class HTTPRequest {
    private int statusCode;
    private String body;

    public HTTPRequest(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }
}
