package org.pickaid.pibrary.api.jei;

import java.util.Objects;
import net.minecraft.network.chat.Component;

public record PiJeiCategorySpec<R>(
        PiJeiRecipeTypeKey<R> recipeType,
        Component title,
        int width,
        int height,
        int sortOrder
) {
    public PiJeiCategorySpec {
        Objects.requireNonNull(recipeType, "recipeType");
        Objects.requireNonNull(title, "title");
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("width and height must be >= 1");
        }
    }
}
