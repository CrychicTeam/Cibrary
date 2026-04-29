package org.pickaid.pibrary.api.jei;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.util.PiIds;
import org.pickaid.pibrary.dev.example.CounterRecipeViews;

class PiJeiRecipeSourceSpecTest {
    @Test
    void linksNeutralCategoryToPibraryRecipeSource() {
        PiJeiRecipeTypeKey<CounterRecipeViews.CounterRecipe> type =
                new PiJeiRecipeTypeKey<>(
                        PiIds.id("pibrary", "counter_charging"),
                        CounterRecipeViews.CounterRecipe.class);
        PiJeiCategorySpec<CounterRecipeViews.CounterRecipe> category =
                new PiJeiCategorySpec<>(type, Component.literal("Counter Charging"), 116, 54, 0);
        PiJeiRecipeSourceSpec<CounterRecipeViews.CounterRecipe> source =
                new PiJeiRecipeSourceSpec<>(type, CounterRecipeViews.SOURCE);

        assertEquals(type, category.recipeType());
        assertEquals(1, source.source().views(null).size());
    }
}
