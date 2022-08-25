package com.github.shiwa.pokedense;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class PokemonsHttpHandler implements HttpHandler {

    public static final String PATH = "/api/v1/";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        assert exchange.getRequestURI().getPath().startsWith(PATH);

        try {
            var action = exchange.getRequestURI().getPath().substring(PATH.length());
            var queryParams = URLEncodedUtils.parse(exchange.getRequestURI(), Charset.defaultCharset()).stream().collect(Collectors.toMap(it -> it.getName(), it -> it.getValue()));

            final String response;
            final String contentType;
            final int code;

            switch (action) {
                case "pokemons" -> {
                    code = 200;
                    List<Pokemon> pokemons = Pokemons.search(queryParams);
                    response = paginateJson(pokemons, Pokemon::toJson, queryParams.get("page"));
                    contentType = "application/json";
                }
                default -> {
                    code = 404;
                    response = "404 (Not Found)\n";
                    contentType = "text/plain";
                }
            }

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(code, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            System.err.println(e);
            throw e;
        }
    }

    private static <T> String paginateJson(
            List<T> allRecords,
            BiConsumer<T, ObjectNode> jsonBuilder,
            String pageParam
    ) throws JsonProcessingException {
        int page;
        try {
            page = Integer.parseInt(pageParam);
        } catch (NumberFormatException e) {
            page = 1;
        }
        page = Math.max(1, page);

        int limit = 20;
        int start = (page - 1) * limit;

        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode array = mapper.createArrayNode();
        allRecords.stream()
                .skip(start)
                .limit(limit)
                .forEach(it -> jsonBuilder.accept(it, array.addObject()));

        final var result = mapper.createObjectNode()
                .put("start", start)
                .put("limit", limit)
                .put("total", allRecords.size())
                .set("records", array);

        return mapper.writeValueAsString(result);
    }


}
