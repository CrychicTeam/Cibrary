package org.pickaid.pibrary.api.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.text.PiTexts;
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

    @Test
    void recipeLayoutCopiesSlotsAndFiltersByRole() {
        PiRecipeView<String> view = new PiRecipeView<>(
                PiIds.id("test", "smelt_dust"),
                "recipe",
                List.of(new PiRecipeIngredient<>(PiRecipeRole.INPUT, "dust")));
        List<PiRecipeSlot<?>> slots = new ArrayList<>();
        slots.add(new PiRecipeSlot<>(PiRecipeRole.INPUT, 18, 8, "dust"));
        slots.add(new PiRecipeSlot<>(PiRecipeRole.OUTPUT, 84, 8, "ingot"));

        PiRecipeLayout<String> layout = new PiRecipeLayout<>(view, slots);
        slots.clear();

        assertEquals(view, layout.view());
        assertEquals(1, layout.byRole(PiRecipeRole.INPUT).count());
        assertEquals(1, layout.byRole(PiRecipeRole.OUTPUT).count());
        assertThrows(UnsupportedOperationException.class,
                () -> layout.slots().add(new PiRecipeSlot<>(PiRecipeRole.DISPLAY, 0, 0, "extra")));
    }

    @Test
    void recipeSlotsRejectNegativeCoordinates() {
        assertThrows(IllegalArgumentException.class,
                () -> new PiRecipeSlot<>(PiRecipeRole.INPUT, -1, 0, "dust"));
        assertThrows(IllegalArgumentException.class,
                () -> new PiRecipeSlot<>(PiRecipeRole.INPUT, 0, -1, "dust"));
    }

    @Test
    void layoutBuilderCreatesReadableRowsAndRelativeSlots() {
        PiRecipeView<String> view = new PiRecipeView<>(
                PiIds.id("test", "charge"),
                "recipe",
                List.of());

        PiRecipeLayout<String> layout = PiRecipeLayout.builder(view)
                .input(18, 18, "dust")
                .thenOutput(64, "core")
                .displayBelow(8, "40 ticks")
                .row(PiRecipeRole.CATALYST, 18, 46, 20, List.of("redstone", "quartz"))
                .build();

        assertEquals(5, layout.slots().size());
        assertEquals("core", layout.slots().get(1).value());
        assertEquals(82, layout.slots().get(1).x());
        assertEquals(18, layout.slots().get(1).y());
        assertEquals(82, layout.slots().get(2).x());
        assertEquals(44, layout.slots().get(2).y());
        assertEquals("quartz", layout.slots().get(4).value());
        assertEquals(38, layout.slots().get(4).x());
    }

    @Test
    void recipeSlotsSupportComplexViewerHints() {
        PiRecipeSlot<?> slot = PiRecipeSlot.builder(PiRecipeRole.INPUT, 18, 18)
                .name("alloy_input")
                .value("copper")
                .value("tin")
                .focusGroup("alloy")
                .standardBackground()
                .fluidRenderer(1000, true, 16, 48)
                .tooltip(PiTexts.tooltip("test", "accepts_alloy"))
                .hiddenLookup()
                .build();

        assertEquals("alloy_input", slot.name().orElseThrow());
        assertEquals(List.of("copper", "tin"), slot.values());
        assertEquals("copper", slot.value());
        assertEquals("alloy", slot.focusGroup().orElseThrow());
        assertEquals(PiRecipeSlot.Background.STANDARD, slot.background());
        assertEquals(new PiRecipeFluidRenderHint(1000, true, 16, 48), slot.fluidRenderer().orElseThrow());
        assertEquals(List.of(Component.translatable("tooltip.test.accepts_alloy")), slot.tooltips());
        assertEquals(PiRecipeSlot.Visibility.HIDDEN_LOOKUP, slot.visibility());
    }

    @Test
    void recipeSlotsRejectInvalidFluidRendererHints() {
        assertThrows(IllegalArgumentException.class,
                () -> new PiRecipeFluidRenderHint(0, true, 16, 16));
        assertThrows(IllegalArgumentException.class,
                () -> new PiRecipeFluidRenderHint(1000, true, 0, 16));
        assertThrows(IllegalArgumentException.class,
                () -> new PiRecipeFluidRenderHint(1000, true, 16, 0));
    }

    @Test
    void layoutBuilderCanAddMultiValueSlots() {
        PiRecipeView<String> view = new PiRecipeView<>(
                PiIds.id("test", "alloy"),
                "recipe",
                List.of());

        PiRecipeLayout<String> layout = PiRecipeLayout.builder(view)
                .slot(PiRecipeSlot.builder(PiRecipeRole.INPUT, 18, 18)
                        .values(List.of("copper", "tin"))
                        .focusGroup("primary")
                        .build())
                .thenOutput(64, "alloy")
                .build();

        assertEquals(List.of("copper", "tin"), layout.slots().get(0).values());
        assertEquals("primary", layout.slots().get(0).focusGroup().orElseThrow());
        assertEquals("alloy", layout.slots().get(1).value());
    }
}
