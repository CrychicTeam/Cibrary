package org.pickaid.pibrary.dev.example;

import java.util.List;
import org.pickaid.pibrary.api.recipe.PiRecipeIngredient;
import org.pickaid.pibrary.api.recipe.PiRecipeRole;
import org.pickaid.pibrary.api.recipe.PiRecipeSource;
import org.pickaid.pibrary.api.recipe.PiRecipeView;
import org.pickaid.pibrary.api.util.PiIds;

/**
 * Small sample recipe source shared by machine logic and viewer specs.
 */
public final class CounterRecipeViews {
    public static final CounterRecipe CHARGE =
            new CounterRecipe("counter_dust", "counter_core", 40);

    public static final PiRecipeSource<CounterRecipe> SOURCE =
            level -> List.of(view(CHARGE));

    private CounterRecipeViews() {
    }

    public static PiRecipeView<CounterRecipe> view(CounterRecipe recipe) {
        return new PiRecipeView<>(
                PiIds.id("pibrary", "counter_charge"),
                recipe,
                List.of(
                        new PiRecipeIngredient<>(PiRecipeRole.INPUT, recipe.input()),
                        new PiRecipeIngredient<>(PiRecipeRole.OUTPUT, recipe.output()),
                        new PiRecipeIngredient<>(PiRecipeRole.DISPLAY, recipe.ticks())
                ));
    }

    public record CounterRecipe(String input, String output, int ticks) {
    }
}
