package org.pickaid.pibrary.api.text;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Static image part for rich text flows.
 *
 * <p>The texture is a normal Minecraft resource. Width and height are display
 * dimensions in GUI pixels. Renderers may choose how to bind and draw the
 * texture, while non-rich contexts use {@link #fallback()}.</p>
 *
 * @param texture texture resource
 * @param width display width in pixels
 * @param height display height in pixels
 * @param altText vanilla fallback text
 */
public record PiImageTextPart(
        ResourceLocation texture,
        int width,
        int height,
        Component altText
) implements PiTextPart {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("pibrary", "image");

    public PiImageTextPart {
        texture = Objects.requireNonNull(texture, "texture");
        requirePositive(width, "width");
        requirePositive(height, "height");
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

    static int requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
        return value;
    }
}
