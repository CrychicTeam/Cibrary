package org.pickaid.pibrary.runtime.recipe;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.level.Level;
import org.pickaid.pibrary.api.recipe.PiRecipeMatch;
import org.pickaid.pibrary.api.recipe.PiRecipeSource;
import org.pickaid.pibrary.api.recipe.PiRecipeView;

/**
 * Small runtime lookup wrapper over a recipe source.
 *
 * @param <R> backing recipe type
 */
public final class PiRecipeLookup<R> {
    private final PiRecipeSource<R> source;

    public PiRecipeLookup(PiRecipeSource<R> source) {
        this.source = Objects.requireNonNull(source, "source");
    }

    public Optional<PiRecipeMatch<R>> find(Level level, Predicate<PiRecipeView<R>> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return source.views(level).stream()
                .filter(predicate)
                .findFirst()
                .map(PiRecipeMatch::new);
    }
}
