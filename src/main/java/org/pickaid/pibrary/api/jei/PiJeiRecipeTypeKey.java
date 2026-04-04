package org.pickaid.pibrary.api.jei;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record PiJeiRecipeTypeKey<R>(
        ResourceLocation id,
        Class<? extends R> recipeClass
) {
    public PiJeiRecipeTypeKey {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(recipeClass, "recipeClass");
    }
}
