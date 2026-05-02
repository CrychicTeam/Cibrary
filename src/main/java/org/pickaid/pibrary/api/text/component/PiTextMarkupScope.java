package org.pickaid.pibrary.api.text.component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.text.PiTexts;

/**
 * Local rules for parsing compact text markup.
 *
 * <p>Use one scope for one kind of content: a spell tooltip, an in-world manual
 * page, a JEI description, or a screen. The scope owns local defaults and local
 * inline handlers, so a special marker such as {@code [mana](example:fireball)}
 * does not become a global language feature by accident.</p>
 */
public final class PiTextMarkupScope {
    private final String namespace;
    private final int imageWidth;
    private final int imageHeight;
    private final int animatedFrames;
    private final int animatedTicksPerFrame;
    private final boolean animatedLoop;
    private final Map<String, PiTextInlineHandler> inlineHandlers;

    private PiTextMarkupScope(Builder builder) {
        this.namespace = builder.namespace;
        this.imageWidth = builder.imageWidth;
        this.imageHeight = builder.imageHeight;
        this.animatedFrames = builder.animatedFrames;
        this.animatedTicksPerFrame = builder.animatedTicksPerFrame;
        this.animatedLoop = builder.animatedLoop;
        this.inlineHandlers = Map.copyOf(builder.inlineHandlers);
    }

    public static PiTextMarkupScope empty() {
        return builder(null).build();
    }

    public static Builder builder(String namespace) {
        return new Builder(namespace);
    }

    public Optional<String> namespace() {
        return Optional.ofNullable(namespace);
    }

    public int imageWidth() {
        return imageWidth;
    }

    public int imageHeight() {
        return imageHeight;
    }

    public int animatedFrames() {
        return animatedFrames;
    }

    public int animatedTicksPerFrame() {
        return animatedTicksPerFrame;
    }

    public boolean animatedLoop() {
        return animatedLoop;
    }

    public Optional<MutableComponent> createInline(PiTextInline inline) {
        Objects.requireNonNull(inline, "inline");
        PiTextInlineHandler handler = inlineHandlers.get(normalize(inline.label()));
        if (handler == null) {
            return Optional.empty();
        }
        return Optional.of(Objects.requireNonNull(handler.create(inline, this), "inline component"));
    }

    public ResourceLocation resolveResource(String target) {
        String cleaned = Objects.requireNonNull(target, "target").trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("resource target must not be blank");
        }
        if (cleaned.indexOf(':') >= 0) {
            return ResourceLocation.parse(cleaned);
        }
        if (namespace == null) {
            return ResourceLocation.parse(cleaned);
        }
        return ResourceLocation.fromNamespaceAndPath(namespace, cleaned);
    }

    static String normalize(String label) {
        String result = Objects.requireNonNull(label, "label").trim().toLowerCase(Locale.ROOT);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("inline label must not be blank");
        }
        return result;
    }

    public static final class Builder {
        private final String namespace;
        private int imageWidth = 16;
        private int imageHeight = 16;
        private int animatedFrames = 1;
        private int animatedTicksPerFrame = 1;
        private boolean animatedLoop = true;
        private final Map<String, PiTextInlineHandler> inlineHandlers = new LinkedHashMap<>();

        private Builder(String namespace) {
            if (namespace == null || namespace.isBlank()) {
                this.namespace = null;
            } else {
                this.namespace = namespace.trim();
            }
            inline("image", this::image);
            inline("img", this::image);
            inline("anim", this::animatedImage);
            inline("animation", this::animatedImage);
            inline("gif", this::animatedImage);
        }

        public Builder imageSize(int width, int height) {
            this.imageWidth = PiImageTextContents.requirePositive(width, "width");
            this.imageHeight = PiImageTextContents.requirePositive(height, "height");
            return this;
        }

        public Builder animatedImage(int frames, int ticksPerFrame, boolean loop) {
            this.animatedFrames = PiImageTextContents.requirePositive(frames, "frames");
            this.animatedTicksPerFrame = PiImageTextContents.requirePositive(ticksPerFrame, "ticksPerFrame");
            this.animatedLoop = loop;
            return this;
        }

        public Builder inline(String label, PiTextInlineHandler handler) {
            Objects.requireNonNull(handler, "handler");
            String normalized = normalize(label);
            PiTextInlineHandler previous = inlineHandlers.put(normalized, handler);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate inline label: " + label);
            }
            return this;
        }

        public PiTextMarkupScope build() {
            return new PiTextMarkupScope(this);
        }

        private MutableComponent image(PiTextInline inline, PiTextMarkupScope scope) {
            PiTextTarget target = PiTextTarget.parse(inline.target());
            int width = target.width().orElse(scope.imageWidth());
            int height = target.height().orElse(scope.imageHeight());
            return PiTextComponents.image(
                    scope.resolveResource(target.resource()),
                    width,
                    height,
                    PiTexts.literal("[" + inline.label() + "]"));
        }

        private MutableComponent animatedImage(PiTextInline inline, PiTextMarkupScope scope) {
            PiTextTarget target = PiTextTarget.parse(inline.target());
            int width = target.width().orElse(scope.imageWidth());
            int height = target.height().orElse(scope.imageHeight());
            int frames = target.intOption("frames").orElse(scope.animatedFrames());
            int ticks = target.intOption("ticks").orElse(scope.animatedTicksPerFrame());
            boolean loop = target.booleanOption("loop").orElse(scope.animatedLoop());
            return PiTextComponents.animatedImage(
                    scope.resolveResource(target.resource()),
                    width,
                    height,
                    frames,
                    ticks,
                    loop,
                    PiTexts.literal("[" + inline.label() + "]"));
        }
    }
}
