package org.pickaid.pibrary.api.jei;

import java.util.List;
import java.util.Objects;

public record PiJeiClickArea(
        String screenClassName,
        int x,
        int y,
        int width,
        int height,
        List<PiJeiRecipeTypeKey<?>> recipeTypes
) {
    public PiJeiClickArea {
        Objects.requireNonNull(screenClassName, "screenClassName");
        Objects.requireNonNull(recipeTypes, "recipeTypes");
        if (screenClassName.isBlank()) {
            throw new IllegalArgumentException("screenClassName must not be blank");
        }
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("x and y must be >= 0");
        }
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("width and height must be >= 1");
        }
        recipeTypes = List.copyOf(recipeTypes);
        if (recipeTypes.isEmpty()) {
            throw new IllegalArgumentException("recipeTypes must not be empty");
        }
    }
}
