package org.pickaid.pibrary.runtime.jei;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingMenu;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.jei.PiJeiBootstrap;
import org.pickaid.pibrary.api.jei.PiJeiCategorySpec;
import org.pickaid.pibrary.api.jei.PiJeiClickArea;
import org.pickaid.pibrary.api.jei.PiJeiRecipeDisplay;
import org.pickaid.pibrary.api.jei.PiJeiRecipeSourceSpec;
import org.pickaid.pibrary.api.jei.PiJeiRecipeTypeKey;
import org.pickaid.pibrary.api.jei.PiJeiTransferSpec;
import org.pickaid.pibrary.api.recipe.PiRecipeLayout;
import org.pickaid.pibrary.api.recipe.PiRecipeRole;
import org.pickaid.pibrary.api.recipe.PiRecipeView;
import org.pickaid.pibrary.dev.example.CounterJeiModule;
import org.pickaid.pibrary.dev.example.CounterRecipeViews;

class PiJeiRecipeCatalogTest {
    @Test
    void catalogReadsViewsAndBackingRecipesFromCollectedSources() {
        PiJeiBootstrap bootstrap = new PiJeiBootstrap();
        bootstrap.registerModule(new CounterJeiModule());

        PiJeiRecipeCatalog catalog = PiJeiRecipeCatalog.from(bootstrap);

        assertTrue(catalog.hasCategory(CounterJeiModule.COUNTER_CHARGING));
        assertTrue(catalog.hasSource(CounterJeiModule.COUNTER_CHARGING));
        assertEquals(List.of(CounterJeiModule.CATEGORY), catalog.categories());
        assertEquals(1, catalog.views(CounterJeiModule.COUNTER_CHARGING, null).size());
        assertEquals(CounterRecipeViews.CHARGE, catalog.recipes(CounterJeiModule.COUNTER_CHARGING, null).get(0));
    }

    @Test
    void catalogBuildsRecipeViewerDisplaysFromViewsAndCategoryLayout() {
        PiJeiBootstrap bootstrap = new PiJeiBootstrap();
        bootstrap.registerModule(new CounterJeiModule());
        PiJeiRecipeCatalog catalog = PiJeiRecipeCatalog.from(bootstrap);

        PiJeiRecipeDisplay<CounterRecipeViews.CounterRecipe> display =
                catalog.displays(CounterJeiModule.COUNTER_CHARGING, null).get(0);

        assertEquals(CounterJeiModule.CATEGORY, display.category());
        assertEquals(CounterRecipeViews.CHARGE, display.view().recipe());
        assertEquals(3, display.layout().slots().size());
    }

    @Test
    void catalogKeepsMultipleSourcesInRegistrationOrder() {
        PiJeiRecipeTypeKey<CounterRecipeViews.CounterRecipe> type = CounterJeiModule.COUNTER_CHARGING;
        CounterRecipeViews.CounterRecipe fast = new CounterRecipeViews.CounterRecipe("dust", "core", 10);
        CounterRecipeViews.CounterRecipe slow = new CounterRecipeViews.CounterRecipe("gem", "core", 80);
        PiJeiBootstrap bootstrap = new PiJeiBootstrap();
        bootstrap.registerCategory(CounterJeiModule.CATEGORY);
        bootstrap.registerRecipeSource(new PiJeiRecipeSourceSpec<>(type, level -> List.of(CounterRecipeViews.view(fast))));
        bootstrap.registerRecipeSource(new PiJeiRecipeSourceSpec<>(type, level -> List.of(CounterRecipeViews.view(slow))));

        PiJeiRecipeCatalog catalog = PiJeiRecipeCatalog.from(bootstrap);

        assertEquals(List.of(fast, slow), catalog.recipes(type, null));
    }

    @Test
    void catalogRejectsSameIdWithIncompatibleRecipeClass() {
        ResourceLocation id = CounterJeiModule.COUNTER_CHARGING.id();
        PiJeiRecipeTypeKey<String> stringType = new PiJeiRecipeTypeKey<>(id, String.class);
        PiJeiBootstrap bootstrap = new PiJeiBootstrap();
        bootstrap.registerCategory(CounterJeiModule.CATEGORY);
        bootstrap.registerRecipeSource(new PiJeiRecipeSourceSpec<>(
                stringType,
                level -> List.of(new PiRecipeView<>(id, "bad", List.of()))));
        PiJeiRecipeCatalog catalog = PiJeiRecipeCatalog.from(bootstrap);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> catalog.views(CounterJeiModule.COUNTER_CHARGING, null));

        assertEquals("recipe source pibrary:counter_charging uses java.lang.String and cannot be read as "
                + CounterRecipeViews.CounterRecipe.class.getName(), exception.getMessage());
    }

    @Test
    void catalogRejectsRecipeSourceWithoutRegisteredCategory() {
        PiJeiBootstrap bootstrap = new PiJeiBootstrap();
        bootstrap.registerRecipeSource(new PiJeiRecipeSourceSpec<>(
                CounterJeiModule.COUNTER_CHARGING,
                CounterRecipeViews.SOURCE));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PiJeiRecipeCatalog.from(bootstrap));

        assertEquals("recipe source pibrary:counter_charging has no registered category", exception.getMessage());
    }

    @Test
    void catalogRejectsDuplicateCategoryIdsWithDifferentRecipeClasses() {
        ResourceLocation id = CounterJeiModule.COUNTER_CHARGING.id();
        PiJeiBootstrap bootstrap = new PiJeiBootstrap();
        bootstrap.registerCategory(CounterJeiModule.CATEGORY);
        bootstrap.registerCategory(new PiJeiCategorySpec<>(
                new PiJeiRecipeTypeKey<>(id, String.class),
                net.minecraft.network.chat.Component.literal("Wrong"),
                16,
                16,
                0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PiJeiRecipeCatalog.from(bootstrap));

        assertEquals("recipe category pibrary:counter_charging is registered for both "
                + CounterRecipeViews.CounterRecipe.class.getName()
                + " and java.lang.String", exception.getMessage());
    }

    @Test
    void catalogRejectsClickAreaWithoutRegisteredCategory() {
        PiJeiBootstrap bootstrap = new PiJeiBootstrap();
        bootstrap.registerClickArea(new PiJeiClickArea(
                "org.pickaid.example.CounterScreen",
                1,
                2,
                18,
                18,
                List.of(CounterJeiModule.COUNTER_CHARGING)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PiJeiRecipeCatalog.from(bootstrap));

        assertEquals("recipe click area pibrary:counter_charging has no registered category", exception.getMessage());
    }

    @Test
    void catalogRejectsTransferWithoutRegisteredCategory() {
        PiJeiBootstrap bootstrap = new PiJeiBootstrap();
        bootstrap.registerTransfer(new PiJeiTransferSpec<>(
                CraftingMenu.class,
                () -> null,
                CounterJeiModule.COUNTER_CHARGING,
                0,
                1,
                1,
                36));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PiJeiRecipeCatalog.from(bootstrap));

        assertEquals("recipe transfer pibrary:counter_charging has no registered category", exception.getMessage());
    }

    @Test
    void categorySpecCanDescribeNeutralRecipeLayoutForCompatAdapters() {
        CounterRecipeViews.CounterRecipe recipe = new CounterRecipeViews.CounterRecipe("dust", "core", 10);
        PiRecipeView<CounterRecipeViews.CounterRecipe> view = CounterRecipeViews.view(recipe);
        PiJeiCategorySpec<CounterRecipeViews.CounterRecipe> category = new PiJeiCategorySpec<>(
                CounterJeiModule.COUNTER_CHARGING,
                net.minecraft.network.chat.Component.literal("Counter Charging"),
                116,
                54,
                0,
                recipeView -> PiRecipeLayout.builder(recipeView)
                        .input(18, 18, recipeView.recipe().input())
                        .thenOutput(64, recipeView.recipe().output())
                        .build());

        PiRecipeLayout<CounterRecipeViews.CounterRecipe> layout = category.layout(view);

        assertEquals(2, layout.slots().size());
        assertEquals(18, layout.byRole(PiRecipeRole.INPUT).findFirst().orElseThrow().x());
        assertEquals("core", layout.byRole(PiRecipeRole.OUTPUT).findFirst().orElseThrow().value());
    }
}
