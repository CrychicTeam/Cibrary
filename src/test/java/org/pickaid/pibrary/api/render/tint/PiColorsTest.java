package org.pickaid.pibrary.api.render.tint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PiColorsTest {
    @Test
    void buildsAndReadsRgbChannels() {
        int color = PiColors.rgb(300, 128, -20);

        assertEquals(0xFF8000, color);
        assertEquals(255, PiColors.red(color));
        assertEquals(128, PiColors.green(color));
        assertEquals(0, PiColors.blue(color));
    }

    @Test
    void mixesAndMultipliesColors() {
        assertEquals(0x800080, PiColors.mix(0xFF0000, 0x0000FF, 0.5D));
        assertEquals(0x402000, PiColors.multiply(0x804000, 0x808080));
    }

    @Test
    void pulseRejectsInvalidPeriod() {
        assertThrows(IllegalArgumentException.class, () -> PiColors.pulse(0, 1, 0.0D, 0.0D));
    }
}
