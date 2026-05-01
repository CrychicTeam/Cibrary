package org.pickaid.pibrary.api.jei;

import java.util.Objects;
import org.pickaid.pibrary.api.recipe.PiRecipeLayout;
import org.pickaid.pibrary.api.recipe.PiRecipeView;

/**
 * Recipe-viewer display object built from a category and one recipe view.
 *
 * <p>A concrete JEI adapter can register this as the JEI recipe object instead
 * of exposing project-specific machine recipes directly.</p>
 *
 * @param category neutral category spec
 * @param view normalized recipe view
 * @param <R> backing recipe type
 */
public record PiJeiRecipeDisplay<R>(
        PiJeiCategorySpec<R> category,
        PiRecipeView<R> view
) {
    public PiJeiRecipeDisplay {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(view, "view");
    }

    public PiRecipeLayout<R> layout() {
        return category.layout(view);
    }
}
