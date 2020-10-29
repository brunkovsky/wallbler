package com.nkoad.wallbler.httpConnector;

public class POSTConnectorElasticsearch extends POSTConnector {

    @Override
    protected String setContentType() {
        return "application/x-ndjson";
    }

}
