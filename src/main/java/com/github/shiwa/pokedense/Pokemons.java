package com.github.shiwa.pokedense;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import tech.units.indriya.ComparableQuantity;
import tech.uom.lib.common.util.NaturalQuantityComparator;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static javax.measure.MetricPrefix.DECI;
import static javax.measure.MetricPrefix.HECTO;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.*;

public final class Pokemons {

    public static final String FILTER_NAME = "name";
    public static final String FILTER_WEIGHT = "weight";
    public static final String FILTER_WEIGHT_OPERATOR = "weightOperator";
    public static final String FILTER_HEIGHT = "height";
    public static final String FILTER_HEIGHT_OPERATOR = "heightOperator";

    private Pokemons() {
        super();
    }

    private static List<Pokemon> pokemons = Collections.emptyList();
    private static Instant lastLoad;
    private static Duration expiration = Duration.of(1, HOURS);

    public static List<Pokemon> search(Map<String, String> params) {

        List<Predicate<Pokemon>> filters = new ArrayList<>();

        final String nameFilter = params.get(FILTER_NAME);
        if (nameFilter != null && !nameFilter.isBlank()) {
            filters.add(it -> it.name.contains(nameFilter));
        }

        final String rawWeightFilter = params.get(FILTER_WEIGHT);
        if (rawWeightFilter != null && !rawWeightFilter.isBlank()) {
            final float weightFilterValue = Float.parseFloat(rawWeightFilter);
            final Unit<Mass> weightFilterUnit = KILOGRAM;
            final ComparableQuantity<Mass> weightFilter = getQuantity(weightFilterValue, weightFilterUnit);
            final String weightFilterOperator = Optional.ofNullable(params.get(FILTER_WEIGHT_OPERATOR)).orElse("=");
            final NaturalQuantityComparator<Mass> comparator = new NaturalQuantityComparator();
            switch (weightFilterOperator) {
                case "<" -> filters.add(it -> comparator.compare(it.weight, weightFilter) < 0);
                case ">" -> filters.add(it -> comparator.compare(it.weight, weightFilter) > 0);
                default -> filters.add(it -> comparator.compare(it.weight, weightFilter) == 0);
            }
        }

        final String rawHeightFilter = params.get(FILTER_HEIGHT);
        if (rawHeightFilter != null && !rawHeightFilter.isBlank()) {
            final float heightFilterValue = Float.parseFloat(params.get(FILTER_HEIGHT));
            final Unit<Length> heightFilterUnit = METRE;
            final ComparableQuantity<Length> heightFilter = getQuantity(heightFilterValue, heightFilterUnit);
            final String heightFilterOperator = Optional.ofNullable(params.get(FILTER_HEIGHT_OPERATOR)).orElse("=");
            final NaturalQuantityComparator<Length> comparator = new NaturalQuantityComparator();
            switch (heightFilterOperator) {
                case "<" -> filters.add(it -> comparator.compare(it.height, heightFilter) < 0);
                case ">" -> filters.add(it -> comparator.compare(it.height, heightFilter) > 0);
                default -> filters.add(it -> comparator.compare(it.height, heightFilter) == 0);
            }
        }


        var pokemons = get().stream();
        for (Predicate<Pokemon> filter : filters) {
            pokemons = pokemons.filter(filter);
        }

        return pokemons.collect(toList());
    }

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
