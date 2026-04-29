package org.pickaid.pibrary.api.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.util.PiIds;

class PiRecipeViewTest {
    @Test
    void copiesIngredientListAndFiltersByRole() {
        List<PiRecipeIngredient<?>> ingredients = new ArrayList<>();
        ingredients.add(new PiRecipeIngredient<>(PiRecipeRole.INPUT, "dust"));
        ingredients.add(new PiRecipeIngredient<>(PiRecipeRole.OUTPUT, "ingot"));

        PiRecipeView<String> view = new PiRecipeView<>(
                PiIds.id("test", "smelt_dust"),
                "recipe",
                ingredients);

        ingredients.clear();

        assertEquals(1, view.byRole(PiRecipeRole.INPUT).count());
        assertEquals(1, view.byRole(PiRecipeRole.OUTPUT).count());
        assertThrows(UnsupportedOperationException.class,
                () -> view.ingredients().add(new PiRecipeIngredient<>(PiRecipeRole.DISPLAY, "extra")));
    }
}
