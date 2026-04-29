package org.pickaid.pibrary.dev.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.recipe.PiRecipeRole;
import org.pickaid.pibrary.runtime.recipe.PiRecipeLookup;
import org.pickaid.pibrary.runtime.recipe.PiRecipeLookupCache;

class CounterRecipeViewsTest {
    @Test
    void exposesStableRecipeView() {
        var view = CounterRecipeViews.view(CounterRecipeViews.CHARGE);

        assertEquals("pibrary:counter_charge", view.id().toString());
        assertEquals(1, view.byRole(PiRecipeRole.INPUT).count());
        assertEquals(1, view.byRole(PiRecipeRole.OUTPUT).count());
        assertEquals(1, view.byRole(PiRecipeRole.DISPLAY).count());
    }

    @Test
    void cachedLookupCanBeClearedByMachineCode() {
        AtomicInteger calls = new AtomicInteger();
        PiRecipeLookupCache<CounterRecipeViews.CounterRecipe> cache =
                new PiRecipeLookupCache<>(new PiRecipeLookup<>(level -> {
                    calls.incrementAndGet();
                    return CounterRecipeViews.SOURCE.views(level);
                }));

        assertTrue(cache.find(null, view -> view.recipe().input().equals("counter_dust")).isPresent());
        assertTrue(cache.find(null, view -> view.recipe().input().equals("counter_dust")).isPresent());
        assertEquals(1, calls.get());

        cache.clear();

        assertTrue(cache.find(null, view -> view.recipe().input().equals("counter_dust")).isPresent());
        assertEquals(2, calls.get());
    }
}
