package com.github.shiwa.pokedense;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import java.util.Objects;

import static tech.units.indriya.unit.Units.KILOGRAM;

public class Pokemon {
    final String name;

    /**
     * In decimetres
     */
    final Quantity<Length> height;
    /**
     * In hectograms
     */
    final Quantity<Mass> weight;

    public Pokemon(String name, Quantity<Length> height, Quantity<Mass> weight) {
        this.name = name;
        this.height = height;
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pokemon pokemon = (Pokemon) o;
        return pokemon.height.equals(height) && pokemon.weight.equals(weight) && name.equals(pokemon.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, height, weight);
    }

    @Override
    public String toString() {
        return name + " (weight:" + weight.toString() + ", height:" + height + ")";
    }
}
