package org.pickaid.pibrary.runtime.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import org.pickaid.pibrary.api.recipe.PiRecipeReloadListener;

/**
 * Runtime invalidation hub for recipe-derived caches.
 */
public final class PiRecipeCaches {
    private final Set<PiRecipeReloadListener> listeners =
            Collections.newSetFromMap(new IdentityHashMap<>());

    public AutoCloseable register(PiRecipeReloadListener listener) {
        Objects.requireNonNull(listener, "listener");
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void invalidateAll() {
        for (PiRecipeReloadListener listener : new ArrayList<>(listeners)) {
            listener.onRecipesReloaded();
        }
    }
}
