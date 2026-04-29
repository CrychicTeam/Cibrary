package org.pickaid.pibrary.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class PiIdsTest {
    @Test
    void joinsPathPartsWithoutSilentLowercasing() {
        assertEquals("block/relay_core", PiIds.path("block", "relay_core"));
        assertEquals("textures/block/relay_core.png", PiIds.texturePath("block", "relay_core"));
        assertThrows(IllegalArgumentException.class, () -> PiIds.path("block", "", "relay_core"));
    }

    @Test
    void buildsCommonResourceLocations() {
        ResourceLocation id = PiIds.id("example", "relay_core");

        assertEquals(ResourceLocation.fromNamespaceAndPath("example", "relay_core"), id);
        assertEquals(ResourceLocation.fromNamespaceAndPath("example", "textures/block/relay_core.png"),
                PiIds.blockTexture("example", "relay_core"));
        assertEquals(ResourceLocation.fromNamespaceAndPath("example", "relay_core_on"), PiIds.suffix(id, "_on"));
        assertEquals(ResourceLocation.fromNamespaceAndPath("example", "raw/relay_core"), PiIds.prefix(id, "raw/"));
    }
}
