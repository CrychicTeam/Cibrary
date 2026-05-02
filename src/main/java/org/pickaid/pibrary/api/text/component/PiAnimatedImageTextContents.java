package org.pickaid.pibrary.api.text.component;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Inline animated image content for rich text renderers.
 */
public class PiAnimatedImageTextContents extends PiImageTextContents {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("pibrary", "animated_image");

    private final int frameCount;
    private final int ticksPerFrame;
    private final boolean loop;

    public PiAnimatedImageTextContents(
            ResourceLocation texture,
            int width,
            int height,
            int frameCount,
            int ticksPerFrame,
            boolean loop,
            Component fallback
    ) {
        super(TYPE, texture, width, height, fallback);
        this.frameCount = requirePositive(frameCount, "frameCount");
        this.ticksPerFrame = requirePositive(ticksPerFrame, "ticksPerFrame");
        this.loop = loop;
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }

    public int frameCount() {
        return frameCount;
    }

    public int ticksPerFrame() {
        return ticksPerFrame;
    }

    public boolean loop() {
        return loop;
    }

    @Override
    public String toString() {
        return "pibrary:animated_image{" + texture() + ", " + width() + "x" + height()
                + ", frames=" + frameCount + ", ticks=" + ticksPerFrame + ", loop=" + loop + "}";
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other)
                && other instanceof PiAnimatedImageTextContents contents
                && frameCount == contents.frameCount
                && ticksPerFrame == contents.ticksPerFrame
                && loop == contents.loop;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), frameCount, ticksPerFrame, loop);
    }
}
