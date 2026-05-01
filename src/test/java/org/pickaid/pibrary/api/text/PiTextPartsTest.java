package org.pickaid.pibrary.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class PiTextPartsTest {
    @Test
    void textSequenceAcceptsBuiltInAndCustomParts() {
        PiTextPart custom = new PiTextPart() {
            @Override
            public ResourceLocation type() {
                return id("custom_badge");
            }

            @Override
            public Component fallback() {
                return PiTexts.literal("[badge]");
            }
        };

        PiTextSequence sequence = PiTextParts.sequence(
                PiTextParts.text(PiTexts.literal("Fireball ")),
                custom);

        assertEquals(PiTextSequence.TYPE, sequence.type());
        assertEquals("Fireball [badge]", sequence.fallback().getString());
        assertEquals(2, sequence.parts().size());
        assertThrows(UnsupportedOperationException.class, () -> sequence.parts().add(custom));
    }

    @Test
    void imagePartCarriesTextureSizeAndVanillaFallback() {
        PiImageTextPart image = PiTextParts.image(
                id("textures/gui/spell/fireball.png"),
                32,
                16,
                PiTexts.literal("[fireball icon]"));

        assertEquals(PiImageTextPart.TYPE, image.type());
        assertEquals(id("textures/gui/spell/fireball.png"), image.texture());
        assertEquals(32, image.width());
        assertEquals(16, image.height());
        assertEquals("[fireball icon]", image.fallback().getString());
    }

    @Test
    void animatedImagePartCarriesFrameTimingAndFallback() {
        PiAnimatedImageTextPart image = PiTextParts.animatedImage(
                id("textures/gui/spell/fireball_loop.png"),
                24,
                24,
                8,
                2,
                true,
                PiTexts.literal("[fireball animation]"));

        assertEquals(PiAnimatedImageTextPart.TYPE, image.type());
        assertEquals(24, image.frameWidth());
        assertEquals(24, image.frameHeight());
        assertEquals(8, image.frameCount());
        assertEquals(2, image.ticksPerFrame());
        assertEquals(true, image.loop());
        assertEquals("[fireball animation]", image.fallback().getString());
    }

    @Test
    void visualPartsRequireUsableDimensionsAndFallbacks() {
        assertThrows(IllegalArgumentException.class,
                () -> PiTextParts.image(id("texture"), 0, 16, PiTexts.literal("bad")));
        assertThrows(IllegalArgumentException.class,
                () -> PiTextParts.animatedImage(id("texture"), 16, 16, 0, 1, true, PiTexts.literal("bad")));
        assertThrows(NullPointerException.class,
                () -> new PiTextSequence(List.of((PiTextPart) null)));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("example", path);
    }
}
