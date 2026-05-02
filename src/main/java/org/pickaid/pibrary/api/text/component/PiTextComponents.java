package org.pickaid.pibrary.api.text.component;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.text.PiTexts;

/**
 * Factories and conversion helpers for Pibrary visual text contents.
 */
public final class PiTextComponents {
    private PiTextComponents() {
    }

    public static MutableComponent image(ResourceLocation texture, int width, int height, Component fallback) {
        return new PiImageTextContents(texture, width, height, fallback).component();
    }

    public static MutableComponent animatedImage(
            ResourceLocation texture,
            int width,
            int height,
            int frameCount,
            int ticksPerFrame,
            boolean loop,
            Component fallback
    ) {
        return new PiAnimatedImageTextContents(
                texture,
                width,
                height,
                frameCount,
                ticksPerFrame,
                loop,
                fallback).component();
    }

    /**
     * Converts a component tree containing Pibrary visual contents into a
     * vanilla-only component tree.
     */
    public static MutableComponent vanillaFallback(Component component) {
        Objects.requireNonNull(component, "component");
        MutableComponent result = copyContentsAsVanilla(component.getContents())
                .withStyle(component.getStyle());
        for (Component sibling : component.getSiblings()) {
            result.append(vanillaFallback(sibling));
        }
        return result;
    }

    private static MutableComponent copyContentsAsVanilla(ComponentContents contents) {
        if (contents instanceof PiVisualTextContents visual) {
            return visual.fallback().copy();
        }
        if (contents == ComponentContents.EMPTY) {
            return Component.empty();
        }
        try {
            return MutableComponent.create(contents);
        } catch (RuntimeException ignored) {
            return PiTexts.literal("");
        }
    }
}
