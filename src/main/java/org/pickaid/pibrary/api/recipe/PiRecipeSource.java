package org.pickaid.pibrary.api.recipe;

import java.util.List;
import net.minecraft.world.level.Level;

/**
 * Supplies normalized recipe views for the current runtime context.
 *
 * @param <R> backing recipe type
 */
@FunctionalInterface
public interface PiRecipeSource<R> {
    /**
     * Returns the recipe views visible in the given level context.
     *
     * @param level level context, or null for pure tests and static sources
     * @return recipe views
     */
    List<PiRecipeView<R>> views(Level level);
}
