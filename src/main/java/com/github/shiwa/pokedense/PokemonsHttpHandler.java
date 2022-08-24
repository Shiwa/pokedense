package com.github.shiwa.pokedense;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public class PokemonsHttpHandler implements HttpHandler {

    public static final String PATH = "/api/v1/";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        assert exchange.getRequestURI().getPath().startsWith(PATH);

        var action = exchange.getRequestURI().getPath().substring(PATH.length());
        var queryParams = URLEncodedUtils.parse(exchange.getRequestURI(), Charset.defaultCharset()).stream().collect(Collectors.toMap(it -> it.getName(), it -> it.getValue()));

        final String response;
        final int code;

        switch (action) {
            case "pokemons" -> {
                code = 200;
                response = formatToJSON(Pokemons.search(queryParams));
                exchange.getResponseHeaders().set("Content-Type", "application/json");
            }
            default -> {
                code = 404;
                response = "404 (Not Found)\n";
            }
        }

        exchange.sendResponseHeaders(code, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static String formatToJSON(List<Pokemon> pokemons) {
        final String response;
        ObjectMapper mapper = new ObjectMapper();
        final ArrayNode array = mapper.createArrayNode();
        pokemons.forEach(it -> array.addObject()
                .put("name", it.name)
                .put("weight", it.weight.getValue().floatValue())
                .put("weightUnit", it.weight.getUnit().toString())
                .put("height", it.height.getValue().floatValue())
                .put("heightUnit", it.height.getUnit().toString())
        );

        try {
            response = mapper.writeValueAsString(array);
        } catch (JsonProcessingException e) {
            System.err.println("Could not format pokemons list to JSON");
            throw new RuntimeException(e);
        }
        return response;
    }


}
