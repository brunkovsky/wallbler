package com.nkoad.wallbler.httpConnector;

import java.net.HttpURLConnection;
import java.net.ProtocolException;

public class GETConnector extends HTTPConnector {

    @Override
    void setRequestMethod(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("GET");
    }

}
