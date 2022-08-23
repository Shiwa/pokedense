package com.github.shiwa.pokedense;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Main class. Read config from CLI and start HTTP server.
 */
public class App {

    private static final int DEFAULT_PORT = 8080;
    public static final String HOSTNAME = "localhost";

    public static void main(String[] args) {
        try {
            final Integer port = getPort(args);
            startServer(port);
            final URL rootUrl = new URL("http", HOSTNAME, port, ResourcesHttpHandler.PATH + "index.html");
            System.out.println("Server successfully started, you can go to " + rootUrl);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private static Integer getPort(String[] args) {
        final int port;
        if (args.length == 1) {
            final String rawPort = args[0];
            try {
                port = Integer.parseInt(rawPort);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Could not parse port number " + rawPort);
            }
        } else {
            port = DEFAULT_PORT;
        }
        return port;
    }

    private static void startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(HOSTNAME, port), 0);

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        server.setExecutor(threadPoolExecutor);

        server.createContext(PokemonsHttpHandler.PATH, new PokemonsHttpHandler());

        server.createContext(ResourcesHttpHandler.PATH, new ResourcesHttpHandler());

        server.start();
    }
}
