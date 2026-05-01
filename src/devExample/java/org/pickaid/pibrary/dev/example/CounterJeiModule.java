package org.pickaid.pibrary.dev.example;

import net.minecraft.network.chat.Component;
import org.pickaid.pibrary.api.jei.PiJeiBridge;
import org.pickaid.pibrary.api.jei.PiJeiCategorySpec;
import org.pickaid.pibrary.api.jei.PiJeiModule;
import org.pickaid.pibrary.api.jei.PiJeiRecipeSourceSpec;
import org.pickaid.pibrary.api.jei.PiJeiRecipeTypeKey;
import org.pickaid.pibrary.api.recipe.PiRecipeLayout;
import org.pickaid.pibrary.api.recipe.PiRecipeRole;
import org.pickaid.pibrary.api.util.PiIds;

/**
 * Dev-only recipe-viewer module for the counter examples.
 */
public final class CounterJeiModule implements PiJeiModule {
    public static final PiJeiRecipeTypeKey<CounterRecipeViews.CounterRecipe> COUNTER_CHARGING =
            new PiJeiRecipeTypeKey<>(
                    PiIds.id("pibrary", "counter_charging"),
                    CounterRecipeViews.CounterRecipe.class);

    public static final PiJeiCategorySpec<CounterRecipeViews.CounterRecipe> CATEGORY =
            new PiJeiCategorySpec<>(COUNTER_CHARGING, Component.literal("Counter Charging"), 116, 54, 0,
                    view -> PiRecipeLayout.builder(view)
                            .input(18, 18, view.recipe().input())
                            .thenOutput(64, view.recipe().output())
                            .display(52, 18, view.recipe().ticks())
                            .build());

    public static final PiJeiRecipeSourceSpec<CounterRecipeViews.CounterRecipe> SOURCE =
            new PiJeiRecipeSourceSpec<>(COUNTER_CHARGING, CounterRecipeViews.SOURCE);

    @Override
    public void contribute(PiJeiBridge bridge) {
        bridge.registerCategory(CATEGORY);
        bridge.registerRecipeSource(SOURCE);
    }
}
