package org.pickaid.pibrary.api.text.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.text.PiTexts;
import org.pickaid.pibrary.api.text.render.PiVisualTextRenderers;

class PiVisualTextContentsTest {
    @Test
    void visualContentsAreComponentContentsWithStableValueIdentity() {
        PiImageTextContents first = new PiImageTextContents(
                new ResourceLocation("example:textures/gui/spell/fireball.png"),
                24,
                12,
                PiTexts.literal("[fireball]"));
        PiImageTextContents second = new PiImageTextContents(
                new ResourceLocation("example:textures/gui/spell/fireball.png"),
                24,
                12,
                PiTexts.literal("[fireball]"));
        PiImageTextContents different = new PiImageTextContents(
                new ResourceLocation("example:textures/gui/spell/ice.png"),
                24,
                12,
                PiTexts.literal("[ice]"));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
        assertEquals(24, first.width());
        assertEquals(12, first.height());
        assertEquals("[fireball]", first.component().getString());
    }

    @Test
    void defaultRendererRegistryKnowsBuiltInVisualContentsAndRejectsDuplicates() {
        PiVisualTextRenderers.Registry registry = PiVisualTextRenderers.defaults();

        assertEquals(true, registry.has(PiImageTextContents.TYPE));
        assertEquals(true, registry.has(PiAnimatedImageTextContents.TYPE));
        assertThrows(IllegalArgumentException.class, () -> registry.register(
                PiImageTextContents.TYPE,
                PiImageTextContents.class,
                (context, contents) -> {
                }));
    }

    @Test
    void customVisualContentsCanRegisterTheirOwnRenderer() {
        PiVisualTextRenderers.Registry registry = new PiVisualTextRenderers.Registry();
        ResourceLocation type = new ResourceLocation("example:badge");

        registry.register(type, BadgeContents.class, (context, contents) -> {
        });

        assertEquals(true, registry.has(type));
    }

    private static final class BadgeContents extends PiVisualTextContents {
        private BadgeContents(ResourceLocation type, Component fallback) {
            super(type, fallback);
        }

        @Override
        public int width() {
            return 8;
        }

        @Override
        public int height() {
            return 8;
        }
    }
}
