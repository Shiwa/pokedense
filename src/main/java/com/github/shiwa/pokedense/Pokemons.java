package com.github.shiwa.pokedense;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.stream.Collectors.toUnmodifiableList;
import static javax.measure.MetricPrefix.DECI;
import static javax.measure.MetricPrefix.HECTO;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.GRAM;
import static tech.units.indriya.unit.Units.METRE;

public final class Pokemons {

    private Pokemons() {
        super();
    }

    private static List<Pokemon> pokemons = Collections.emptyList();
    private static Instant lastLoad;
    private static Duration expiration = Duration.of(1, HOURS);

    public static List<Pokemon> get() {
        final boolean needToLoad = lastLoad == null || now().isAfter(lastLoad.plus(expiration));
        if (needToLoad) {
            fetchFromPokeAPI();
            lastLoad = now();
        }
        return pokemons;
    }

    /**
     *
     */
    private static void fetchFromPokeAPI() {
        try {
            var client = HttpClient.newHttpClient();

            final URI allPokemonsURL = URI.create("https://pokeapi.co/api/v2/pokemon/?limit=2000");
            var listRequest = HttpRequest.newBuilder(allPokemonsURL)
                    .header("accept", "application/json")
                    .build();

            final HttpResponse.BodyHandler<Supplier<List<String>>> pokemonListReader = buildPokemonUrlsBodyHandler();
            var pokemonUrls = client.send(listRequest, pokemonListReader).body().get();

            final HttpResponse.BodyHandler<Supplier<Pokemon>> pokemonReader = buildPokemonBodyHandler();

            pokemons = pokemonUrls.stream().parallel()
                    .map(detailsUrl -> {
                        try {
                            final HttpRequest pokemonDetailsRequest = HttpRequest.newBuilder(URI.create(detailsUrl))
                                    .header("accept", "application/json")
                                    .build();
                            return client.send(
                                    pokemonDetailsRequest,
                                    pokemonReader
                            ).body().get();
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(toUnmodifiableList());
        } catch (Exception e) {
            System.err.println("Could not retrieve pokemon list: " + e);
        }
    }

    private static HttpResponse.BodyHandler<Supplier<List<String>>> buildPokemonUrlsBodyHandler() {
        final HttpResponse.BodyHandler<Supplier<List<String>>> pokemonListReader = responseInfo -> {
            BodySubscriber<InputStream> upstream = BodySubscribers.ofInputStream();

            BodySubscriber<Supplier<List<String>>> downstream = BodySubscribers.mapping(
                    upstream,
                    (InputStream is) -> () -> {
                        try (InputStream stream = is) {
                            ObjectMapper mapper = new ObjectMapper();
                            final JsonNode root = mapper.readTree(stream);
                            List<String> urls = new ArrayList<>(root.get("count").asInt());
                            root.get("results").elements().forEachRemaining(it -> urls.add(it.get("url").asText()));
                            return urls;
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
            return downstream;
        };
        return pokemonListReader;
    }

    private static HttpResponse.BodyHandler<Supplier<Pokemon>> buildPokemonBodyHandler() {
        class PokemonDeserializer extends StdDeserializer<Pokemon> {
            protected PokemonDeserializer() {
                super(Pokemon.class);
            }

            @Override
            public Pokemon deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                JsonNode node = parser.getCodec().readTree(parser);
                return new Pokemon(
                        node.get("name").asText(),
                        getQuantity(node.get("height").asInt(), DECI(METRE)),
                        getQuantity(node.get("weight").asInt(), HECTO(GRAM))
                );
            }
        }

        final HttpResponse.BodyHandler<Supplier<Pokemon>> pokemonReader = responseInfo -> {
            BodySubscriber<InputStream> upstream = BodySubscribers.ofInputStream();

            BodySubscriber<Supplier<Pokemon>> downstream = BodySubscribers.mapping(
                    upstream,
                    (InputStream is) -> () -> {
                        try (InputStream stream = is) {
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                            SimpleModule module =
                                    new SimpleModule("PokemonDeserializer", new Version(1, 0, 0, null, null, null));
                            module.addDeserializer(Pokemon.class, new PokemonDeserializer());
                            mapper.registerModule(module);
                            return mapper.readValue(stream, Pokemon.class);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
            return downstream;
        };
        return pokemonReader;
    }
}
