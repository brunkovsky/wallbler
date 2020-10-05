package com.nkoad.wallbler.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HTTPConnectorHelper {

    public HTTPRequest httpGetRequest(String url) throws IOException {
        return httpRequest(url, "GET");
    }

    public HTTPRequest httpPostRequest(String url) throws IOException {
        return httpRequest(url, "POST");
    }

    public HTTPRequest httpPutRequest(String url) throws IOException {
        return httpRequest(url, "PUT");
    }

    public HTTPRequest httpDeleteRequest(String url) throws IOException {
        return httpRequest(url, "DELETE");
    }

    private HTTPRequest httpRequest(String url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.connect();
        InputStreamReader in = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader buffer = new BufferedReader(in);
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = buffer.readLine())!= null) {
            sb.append(line);
        }
        connection.disconnect();
        buffer.close();
        in.close();
        return new HTTPRequest(connection.getResponseCode(), sb.toString());
    }

}
