package org.pickaid.pibrary.api.text;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Factories for built-in rich text parts.
 */
public final class PiTextParts {
    private PiTextParts() {
    }

    public static PiComponentTextPart text(Component component) {
        return new PiComponentTextPart(component);
    }

    public static PiImageTextPart image(ResourceLocation texture, int width, int height, Component altText) {
        return new PiImageTextPart(texture, width, height, altText);
    }

    public static PiAnimatedImageTextPart animatedImage(
            ResourceLocation texture,
            int frameWidth,
            int frameHeight,
            int frameCount,
            int ticksPerFrame,
            boolean loop,
            Component altText
    ) {
        return new PiAnimatedImageTextPart(
                texture,
                frameWidth,
                frameHeight,
                frameCount,
                ticksPerFrame,
                loop,
                altText);
    }

    public static PiTextSequence sequence(PiTextPart... parts) {
        Objects.requireNonNull(parts, "parts");
        return PiTextSequence.of(parts);
    }
}
