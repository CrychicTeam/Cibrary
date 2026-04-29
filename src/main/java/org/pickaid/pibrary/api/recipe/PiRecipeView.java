package org.pickaid.pibrary.api.recipe;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;

/**
 * Stable recipe data shared by machine logic and recipe-viewer integration.
 *
 * @param id stable recipe view id
 * @param recipe backing recipe object
 * @param ingredients normalized ingredient roles
 * @param <R> backing recipe type
 */
public record PiRecipeView<R>(
        ResourceLocation id,
        R recipe,
        List<PiRecipeIngredient<?>> ingredients
) {
    public PiRecipeView {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(recipe, "recipe");
        ingredients = List.copyOf(Objects.requireNonNull(ingredients, "ingredients"));
    }

    /**
     * Returns ingredient values with the requested role.
     *
     * @param role role to select
     * @return matching ingredients
     */
    public Stream<PiRecipeIngredient<?>> byRole(PiRecipeRole role) {
        Objects.requireNonNull(role, "role");
        return ingredients.stream().filter(ingredient -> ingredient.role() == role);
    }
}
