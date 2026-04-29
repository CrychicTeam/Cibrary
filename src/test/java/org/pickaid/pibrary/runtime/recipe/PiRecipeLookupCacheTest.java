package org.pickaid.pibrary.runtime.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.recipe.PiRecipeIngredient;
import org.pickaid.pibrary.api.recipe.PiRecipeRole;
import org.pickaid.pibrary.api.recipe.PiRecipeSource;
import org.pickaid.pibrary.api.recipe.PiRecipeView;
import org.pickaid.pibrary.api.util.PiIds;

class PiRecipeLookupCacheTest {
    @Test
    void reusesCachedMatchUntilCleared() {
        AtomicInteger calls = new AtomicInteger();
        PiRecipeSource<String> source = level -> {
            calls.incrementAndGet();
            return List.of(new PiRecipeView<>(
                    PiIds.id("test", "charge"),
                    "charge",
                    List.of(new PiRecipeIngredient<>(PiRecipeRole.INPUT, "dust"))));
        };

        PiRecipeLookupCache<String> cache =
                new PiRecipeLookupCache<>(new PiRecipeLookup<>(source));

        assertTrue(cache.find(null, view -> view.recipe().equals("charge")).isPresent());
        assertTrue(cache.find(null, view -> view.recipe().equals("charge")).isPresent());
        assertEquals(1, calls.get());

        cache.clear();
        assertTrue(cache.find(null, view -> view.recipe().equals("charge")).isPresent());
        assertEquals(2, calls.get());
    }
}
