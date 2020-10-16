package com.nkoad.wallbler.httpConnector;

import java.net.HttpURLConnection;
import java.net.ProtocolException;

public class POSTConnector extends HTTPConnector {

    @Override
    void setRequestMethod(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("POST");
    }

}
