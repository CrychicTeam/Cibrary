package org.pickaid.pibrary.api.recipe;

import java.util.Objects;

/**
 * One ingredient-like value in a normalized recipe view.
 *
 * @param role how the value participates in the recipe
 * @param value item, stack, id, or project-owned recipe value
 * @param <T> value type
 */
public record PiRecipeIngredient<T>(PiRecipeRole role, T value) {
    public PiRecipeIngredient {
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(value, "value");
    }
}
