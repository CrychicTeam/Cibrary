package org.pickaid.pibrary.api.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.presentation.PiPresentationContext;
import org.pickaid.pibrary.api.presentation.PiPresentationSource;
import org.pickaid.pibrary.api.presentation.PiPresentations;

class PiMenuDataTest {
    @Test
    void mutableSlotsExposeVanillaContainerDataShape() {
        Counter counter = new Counter();
        PiMenuData data = PiMenuData.create(PiMenuDataSlot.mutable(() -> counter.value, value -> counter.value = value));

        assertEquals(1, data.getCount());
        assertEquals(0, data.get(0));

        data.set(0, 7);

        assertEquals(7, counter.value);
        assertEquals(7, data.get(0));
    }

    @Test
    void invalidSlotIndexFailsFast() {
        PiMenuData data = PiMenuData.create(PiMenuDataSlot.readOnly(() -> 1));

        assertThrows(IndexOutOfBoundsException.class, () -> data.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> data.set(-1, 2));
    }

    @Test
    void refreshingMenuDataInvalidatesMarkedScreenProjection() {
        ScreenSource source = new ScreenSource();
        PiMenuData data = PiMenuData.refreshing(
                source,
                PiMenuDataSlot.mutable(() -> source.value, value -> source.value = value)
        );

        assertEquals(0, PiPresentations.screens().resolve(source, Integer.class, 0.0F));

        source.value = 3;
        assertEquals(0, PiPresentations.screens().resolve(source, Integer.class, 0.0F));

        data.set(0, 9);

        assertEquals(9, PiPresentations.screens().resolve(source, Integer.class, 0.0F));
    }

    private static final class Counter {
        private int value;
    }

    private static final class ScreenSource implements PiPresentationSource {
        private int value;

        @Override
        public void contributePresentation(PiPresentationContext context) {
            context.screens().snapshot(Integer.class, ignored -> value);
            context.screens().refreshOnMenuData(Integer.class);
        }
    }
}
