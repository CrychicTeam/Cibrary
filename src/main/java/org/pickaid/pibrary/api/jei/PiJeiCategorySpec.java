package org.pickaid.pibrary.api.jei;

import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import org.pickaid.pibrary.api.recipe.PiRecipeLayout;
import org.pickaid.pibrary.api.recipe.PiRecipeView;

/**
 * Neutral recipe category description.
 *
 * @param recipeType recipe type key
 * @param title visible category title
 * @param width display width
 * @param height display height
 * @param sortOrder relative sort order
 * @param layoutFactory neutral recipe layout factory
 * @param <R> recipe type
 */
public record PiJeiCategorySpec<R>(
        PiJeiRecipeTypeKey<R> recipeType,
        Component title,
        int width,
        int height,
        int sortOrder,
        Function<PiRecipeView<R>, PiRecipeLayout<R>> layoutFactory
) {
    public PiJeiCategorySpec {
        Objects.requireNonNull(recipeType, "recipeType");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(layoutFactory, "layoutFactory");
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("width and height must be >= 1");
        }
    }

    public PiJeiCategorySpec(
            PiJeiRecipeTypeKey<R> recipeType,
            Component title,
            int width,
            int height,
            int sortOrder
    ) {
        this(recipeType, title, width, height, sortOrder, view -> new PiRecipeLayout<>(view, java.util.List.of()));
    }

    public PiRecipeLayout<R> layout(PiRecipeView<R> view) {
        return layoutFactory.apply(Objects.requireNonNull(view, "view"));
    }
}
