package com.github.shiwa.pokedense;

import net.jqwik.api.*;
import net.jqwik.api.constraints.FloatRange;
import net.jqwik.api.constraints.WithNull;
import org.junit.jupiter.api.Test;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static tech.units.indriya.unit.Units.METRE;


public class PokemonsTest {

    @Test
    public void testGetReturnsAllPokemons() {

        final List<Pokemon> pokemons = Pokemons.get();

        assertEquals(pokemons.size(), 1_154);

        final Pokemon[] matchingPikachu = pokemons.stream()
                .filter(it -> Objects.equals(it.name, "pikachu"))
                .toArray(Pokemon[]::new);
        assertEquals(matchingPikachu.length, 1);
        final Pokemon pikachu = matchingPikachu[0];
        assertTrue(pikachu.height.isEquivalentTo(getQuantity("40 cm").asType(Length.class)));
        assertTrue(pikachu.weight.isEquivalentTo(getQuantity("6 kg").asType(Mass.class)));
    }

    @Property(tries = 100)
    public void searchResultsMatchCriteria(
            @ForAll @WithNull String name,
            @ForAll @FloatRange(min = 0, max = 1000) @WithNull Float weight,
            @ForAll("filterOperator") String weightOperator,
            @ForAll @FloatRange(min = 0, max = 1000) @WithNull Float height,
            @ForAll("filterOperator") String heightOperator
    ) {
        var params = Map.of(
                Pokemons.FILTER_NAME, name != null ? name : "",
                Pokemons.FILTER_WEIGHT, weight != null ? weight.toString() : "",
                Pokemons.FILTER_WEIGHT_OPERATOR, weightOperator,
                Pokemons.FILTER_HEIGHT, height != null ? height.toString() : "",
                Pokemons.FILTER_HEIGHT_OPERATOR, heightOperator
        );

        var results = Pokemons.search(params);

        assertTrue(results.stream().allMatch(
                it -> name == null || name.isBlank() || it.name.contains(name)
        ));

        for (Pokemon pokemon : results) {
            final var pokeWeight = pokemon.weight.to(KILOGRAM).getValue().floatValue();
            final boolean matchWeightFilter = weight == null || compare(weight, weightOperator, pokeWeight);
            assertTrue(matchWeightFilter, () -> "Pokemon " + pokemon + " does not match weight criteria " + weightOperator + " " + weight + " kg");

            final var pokeHeight = pokemon.height.to(METRE).getValue().floatValue();
            final boolean matchHeightFilter = height == null || compare(height, heightOperator, pokeHeight);
            assertTrue(matchHeightFilter, () -> "Pokemon " + pokemon + " does not match height criteria " + heightOperator + " " + height + " m");
        }
    }

    private static boolean compare(float a, String operator, float b) {
        final float threshold = 0.001f;
        return operator.equals(">") ? b + threshold > a
                : operator.equals("<") ? b - threshold < a
                : Math.abs(b - a) < threshold;
    }

    @Provide
    Arbitrary<String> filterOperator() {
        return Arbitraries.strings().withChars("<>=").ofLength(1);
    }
}
