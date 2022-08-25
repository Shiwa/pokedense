package com.github.shiwa.pokedense;

import com.fasterxml.jackson.databind.node.ObjectNode;

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

    void toJson(ObjectNode jsonObject) {
        jsonObject
                .put("name", this.name)
                .put("weight", this.weight.getValue().floatValue())
                .put("weightUnit", this.weight.getUnit().toString())
                .put("height", this.height.getValue().floatValue())
                .put("heightUnit", this.height.getUnit().toString());
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
