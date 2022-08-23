package com.github.shiwa.pokedense;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import java.util.List;
import java.util.Objects;

import static tech.units.indriya.quantity.Quantities.getQuantity;

public class PokemonsTest
        extends TestCase {
    public PokemonsTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(PokemonsTest.class);
    }

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
}
