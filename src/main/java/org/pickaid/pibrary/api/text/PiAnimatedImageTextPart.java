package org.pickaid.pibrary.api.text;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Animated image part for rich text flows.
 *
 * <p>The default model is a frame strip or sprite sheet handled by the eventual
 * renderer. Pibrary stores timing and fallback data here; render packages can
 * add richer playback, easing, or shader effects on top without replacing this
 * base content contract.</p>
 *
 * @param texture animated texture resource
 * @param frameWidth frame display width in pixels
 * @param frameHeight frame display height in pixels
 * @param frameCount number of frames
 * @param ticksPerFrame duration of one frame
 * @param loop whether playback loops
 * @param altText vanilla fallback text
 */
public record PiAnimatedImageTextPart(
        ResourceLocation texture,
        int frameWidth,
        int frameHeight,
        int frameCount,
        int ticksPerFrame,
        boolean loop,
        Component altText
) implements PiTextPart {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("pibrary", "animated_image");

    public PiAnimatedImageTextPart {
        texture = Objects.requireNonNull(texture, "texture");
        PiImageTextPart.requirePositive(frameWidth, "frameWidth");
        PiImageTextPart.requirePositive(frameHeight, "frameHeight");
        PiImageTextPart.requirePositive(frameCount, "frameCount");
        PiImageTextPart.requirePositive(ticksPerFrame, "ticksPerFrame");
        altText = Objects.requireNonNull(altText, "altText").copy();
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }

    @Override
    public Component fallback() {
        return altText.copy();
    }
}
