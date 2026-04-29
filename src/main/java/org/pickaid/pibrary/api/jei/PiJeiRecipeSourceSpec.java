package org.pickaid.pibrary.api.jei;

import java.util.Objects;
import org.pickaid.pibrary.api.recipe.PiRecipeSource;

/**
 * Neutral link between a viewer category and a Pibrary recipe source.
 *
 * @param recipeType logical recipe type key
 * @param source recipe data source
 * @param <R> recipe type
 */
public record PiJeiRecipeSourceSpec<R>(
        PiJeiRecipeTypeKey<R> recipeType,
        PiRecipeSource<R> source
) {
    public PiJeiRecipeSourceSpec {
        Objects.requireNonNull(recipeType, "recipeType");
        Objects.requireNonNull(source, "source");
    }
}
