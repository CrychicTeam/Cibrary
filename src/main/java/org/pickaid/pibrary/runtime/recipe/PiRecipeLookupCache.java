package org.pickaid.pibrary.runtime.recipe;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.level.Level;
import org.pickaid.pibrary.api.recipe.PiRecipeMatch;
import org.pickaid.pibrary.api.recipe.PiRecipeView;

/**
 * Caches the last matching recipe for machine-like runtime code.
 *
 * @param <R> backing recipe type
 */
public final class PiRecipeLookupCache<R> {
    private final PiRecipeLookup<R> lookup;
    private PiRecipeMatch<R> cached;

    public PiRecipeLookupCache(PiRecipeLookup<R> lookup) {
        this.lookup = Objects.requireNonNull(lookup, "lookup");
    }

    public Optional<PiRecipeMatch<R>> find(Level level, Predicate<PiRecipeView<R>> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        if (cached != null && predicate.test(cached.view())) {
            return Optional.of(cached);
        }
        Optional<PiRecipeMatch<R>> found = lookup.find(level, predicate);
        cached = found.orElse(null);
        return found;
    }

    public void clear() {
        cached = null;
    }
}
