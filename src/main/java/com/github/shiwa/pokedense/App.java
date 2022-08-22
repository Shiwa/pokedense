package com.github.shiwa.pokedense;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Main class. Read config from CLI and start HTTP server.
 */
public class App {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws IOException {
        try {
            final Integer port = getPort(args);
            startServer(port);
        } catch (Exception e) {
            System.err.println(e);
            return;
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
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        server.setExecutor(threadPoolExecutor);

        server.createContext("/api", new PokemonsHttpHandler());

// TODO        server.createContext("/resources");
        server.start();
    }
}
