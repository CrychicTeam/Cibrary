package org.pickaid.pibrary.api.recipe;

/**
 * Callback for recipe cache invalidation after recipe data changes.
 */
@FunctionalInterface
public interface PiRecipeReloadListener {
    void onRecipesReloaded();
}
