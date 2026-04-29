package org.pickaid.pibrary.runtime.creative;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.world.item.CreativeModeTab;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.creative.PiCreativeVisibility;

class PiForgeCreativeTabsTest {
    @Test
    void mapsPibraryVisibilityToForgeTabVisibility() {
        assertEquals(
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS,
                PiForgeCreativeTabs.toForgeVisibility(PiCreativeVisibility.PARENT_AND_SEARCH));
        assertEquals(
                CreativeModeTab.TabVisibility.PARENT_TAB_ONLY,
                PiForgeCreativeTabs.toForgeVisibility(PiCreativeVisibility.PARENT_ONLY));
        assertEquals(
                CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY,
                PiForgeCreativeTabs.toForgeVisibility(PiCreativeVisibility.SEARCH_ONLY));
    }

    @Test
    void hiddenEntriesMustBeFilteredBeforeForgeOutput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PiForgeCreativeTabs.toForgeVisibility(PiCreativeVisibility.HIDDEN));
    }
}
