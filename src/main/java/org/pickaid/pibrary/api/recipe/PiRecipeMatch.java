package org.pickaid.pibrary.api.recipe;

import java.util.Objects;

/**
 * Result of matching a recipe view for runtime use.
 *
 * @param view matched recipe view
 * @param <R> backing recipe type
 */
public record PiRecipeMatch<R>(PiRecipeView<R> view) {
    public PiRecipeMatch {
        Objects.requireNonNull(view, "view");
    }
}
