package org.pickaid.pibrary.api.registrate;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PiRegistrateTransformsTest {
    @Test
    void rejectsInvalidLightLevel() {
        assertThrows(IllegalArgumentException.class, () -> PiBlockTransforms.lightLevel(-1));
        assertThrows(IllegalArgumentException.class, () -> PiBlockTransforms.lightLevel(16));
    }

    @Test
    void rejectsInvalidItemNumbers() {
        assertThrows(IllegalArgumentException.class, () -> PiItemTransforms.stacksTo(0));
        assertThrows(IllegalArgumentException.class, () -> PiItemTransforms.durability(0));
    }
}
