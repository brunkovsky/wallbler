package com.nkoad.wallbler.httpConnector;

import com.nkoad.wallbler.core.HTTPRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class HTTPConnector {
    abstract protected void setRequestMethod(HttpURLConnection connection) throws IOException;

    public HTTPRequest httpRequest(String url, String payload) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        setRequestMethod(connection);
        setPayload(connection, payload);
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

    public HTTPRequest httpRequest(String url) throws IOException {
        return httpRequest(url, null);
    }

    protected String setContentType() {
        return "application/json; utf-8";
    }

    private void setPayload(HttpURLConnection connection, String payload) throws IOException {
        if (payload == null || payload.isEmpty()) return;
        connection.setRequestProperty("Content-Type", setContentType());
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    }

}
