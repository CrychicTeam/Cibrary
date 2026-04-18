package org.pickaid.pibrary.api.jei;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * Typed logical key for a recipe category understood by a compat runtime.
 *
 * @param id logical recipe type id
 * @param recipeClass concrete recipe class
 * @param <R> recipe type
 */
public record PiJeiRecipeTypeKey<R>(
        ResourceLocation id,
        Class<? extends R> recipeClass
) {
    public PiJeiRecipeTypeKey {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(recipeClass, "recipeClass");
    }
}
