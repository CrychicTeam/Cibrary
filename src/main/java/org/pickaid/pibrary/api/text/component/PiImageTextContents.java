package org.pickaid.pibrary.api.text.component;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Inline static image content for rich text renderers.
 *
 * <p>The texture is a normal Minecraft resource. Width and height are GUI
 * pixels. Vanilla text traversal sees {@link #fallback()} so the same component
 * remains usable in narration, search, logs, simple tooltips, and tests.</p>
 */
public class PiImageTextContents extends PiVisualTextContents {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("pibrary", "image");

    private final ResourceLocation texture;
    private final int width;
    private final int height;

    public PiImageTextContents(ResourceLocation texture, int width, int height, Component fallback) {
        this(TYPE, texture, width, height, fallback);
    }

    protected PiImageTextContents(
            ResourceLocation type,
            ResourceLocation texture,
            int width,
            int height,
            Component fallback
    ) {
        super(type, fallback);
        this.texture = Objects.requireNonNull(texture, "texture");
        this.width = requirePositive(width, "width");
        this.height = requirePositive(height, "height");
    }

    public ResourceLocation texture() {
        return texture;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    static int requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
        return value;
    }

    @Override
    public String toString() {
        return "pibrary:image{" + texture + ", " + width + "x" + height + "}";
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof PiImageTextContents contents
                && getClass() == contents.getClass()
                && baseEquals(contents)
                && texture.equals(contents.texture)
                && width == contents.width
                && height == contents.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), baseHashCode(), texture, width, height);
    }
}
