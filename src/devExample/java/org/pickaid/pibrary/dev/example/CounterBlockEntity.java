package org.pickaid.pibrary.dev.example;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.pickaid.pibrary.api.blockentity.PiStateBlockEntity;
import org.pickaid.pibrary.api.presentation.PiPresentationContext;
import org.pickaid.pibrary.api.presentation.PiPresentationSource;
import org.pickaid.pibrary.api.presentation.PiPresentationScope;
import org.pickaid.pibrary.runtime.recipe.PiRecipeLookup;
import org.pickaid.pibrary.runtime.recipe.PiRecipeLookupCache;

/**
 * Minimal sample block entity using {@link PiStateBlockEntity}.
 */
public final class CounterBlockEntity extends PiStateBlockEntity<CounterState> implements PiPresentationSource {
    private final PiRecipeLookupCache<CounterRecipeViews.CounterRecipe> recipeCache =
            new PiRecipeLookupCache<>(new PiRecipeLookup<>(CounterRecipeViews.SOURCE));

    /**
     * Creates the sample block entity.
     *
     * @param type block entity type
     * @param pos block position
     * @param blockState placed block state
     */
    public CounterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, CounterState.class);
    }

    /**
     * Increments the synced counter field.
     */
    public void increment() {
        updateState(state -> state.count++);
    }

    /**
     * Returns the current synced counter value.
     *
     * @return current counter value
     */
    public int getCount() {
        return viewState().count;
    }

    /**
     * Returns the sample recipe cache a machine-like block entity would use.
     *
     * @return recipe cache
     */
    public PiRecipeLookupCache<CounterRecipeViews.CounterRecipe> recipeCache() {
        return recipeCache;
    }

    @Override
    public void contributePresentation(PiPresentationContext context) {
        context.worldRender().snapshot(
                CounterWorldRenderVisual.class,
                PiPresentationScope.TRACKING,
                partialTick -> CounterWorldRenderVisual.from(viewState())
        );
        context.worldRender().renderDistance(32.0D);
        context.worldRender().refreshOnClientApply(CounterWorldRenderVisual.class);

        context.screens().snapshot(
                CounterScreenModel.class,
                partialTick -> CounterScreenModel.from(viewState())
        );
        context.screens().session(CounterScreenSession.class, CounterScreenSession::new);
        context.screens().refreshOnClientApply(CounterScreenModel.class);
        context.screens().refreshOnMenuData(CounterScreenModel.class);
    }
}
