package com.nkoad.wallbler.httpConnector;

public class POSTConnectorNdjsonContentType extends POSTConnector {

    @Override
    protected String setContentType() {
        return "application/x-ndjson";
    }

}
