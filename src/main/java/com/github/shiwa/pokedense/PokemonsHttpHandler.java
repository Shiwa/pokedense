package com.github.shiwa.pokedense;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class PokemonsHttpHandler implements HttpHandler {

    public static final String PATH = "/api/v1/";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // TODO
    }
}
