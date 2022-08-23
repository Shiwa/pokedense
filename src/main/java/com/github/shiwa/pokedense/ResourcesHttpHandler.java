package com.github.shiwa.pokedense;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.StreamSupport;

/**
 * Quick & dirty Http handler to serve files from resources.
 */
public class ResourcesHttpHandler implements HttpHandler {

    public static final String PATH = "/www/";

    @Override
    public void handle(HttpExchange t) throws IOException {
        assert t.getRequestURI().getPath().startsWith(PATH);

        var path = t.getRequestURI().getPath().substring(PATH.length());

        System.out.println("Resource requested: " + path);

        var fileStream = openResourceAsStream(path);
        if (fileStream == null) {
            String response = "404 (Not Found)\n";
            t.sendResponseHeaders(404, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            final String mime;
            if (path.endsWith(".js")) mime = "application/javascript";
            else if (path.endsWith(".css")) mime = "text/css";
            else mime = "text/html";// default

            Headers h = t.getResponseHeaders();
            h.set("Content-Type", mime);
            t.sendResponseHeaders(200, 0);

            OutputStream os = t.getResponseBody();
            fileStream.transferTo(os);
            os.close();
        }

    }

    private InputStream openResourceAsStream(String path) {
        var folder = "www/";
        return getClass().getClassLoader().getResourceAsStream(folder + path);
    }
}
