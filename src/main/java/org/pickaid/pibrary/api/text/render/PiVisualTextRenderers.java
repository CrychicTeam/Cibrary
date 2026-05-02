package org.pickaid.pibrary.api.text.render;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.text.PiTexts;
import org.pickaid.pibrary.api.text.component.PiAnimatedImageTextContents;
import org.pickaid.pibrary.api.text.component.PiImageTextContents;
import org.pickaid.pibrary.api.text.component.PiVisualTextContents;

/**
 * Registry and helpers for drawing Pibrary visual text contents in GUI code.
 *
 * <p>Vanilla text rendering sees the fallback text of a visual node. Screens or
 * compat layers that want actual inline images should use {@link #drawLine}.
 * The method walks the same Minecraft component tree, renders vanilla contents
 * with the active {@link Font}, and dispatches custom visual contents through
 * this registry.</p>
 */
public final class PiVisualTextRenderers {
    private static final Registry GLOBAL = defaults();

    private PiVisualTextRenderers() {
    }

    public static Registry global() {
        return GLOBAL;
    }

    public static Registry defaults() {
        Registry registry = new Registry();
        registry.register(PiImageTextContents.TYPE, PiImageTextContents.class, PiVisualTextRenderers::drawImage);
        registry.register(
                PiAnimatedImageTextContents.TYPE,
                PiAnimatedImageTextContents.class,
                PiVisualTextRenderers::drawAnimatedImage);
        return registry;
    }

    public static int width(Font font, Component component) {
        return global().width(font, component);
    }

    public static int drawLine(
            GuiGraphics graphics,
            Font font,
            Component component,
            int x,
            int y,
            int color,
            boolean shadow,
            long gameTime
    ) {
        return global().drawLine(graphics, font, component, x, y, color, shadow, gameTime);
    }

    private static void drawImage(PiVisualTextRenderContext context, PiImageTextContents contents) {
        context.graphics().blit(
                contents.texture(),
                context.x(),
                context.y(),
                0,
                0.0F,
                0.0F,
                contents.width(),
                contents.height(),
                contents.width(),
                contents.height());
    }

    private static void drawAnimatedImage(PiVisualTextRenderContext context, PiAnimatedImageTextContents contents) {
        long rawFrame = context.gameTime() / contents.ticksPerFrame();
        int frame = contents.loop()
                ? (int) (rawFrame % contents.frameCount())
                : (int) Math.min(contents.frameCount() - 1L, rawFrame);
        context.graphics().blit(
                contents.texture(),
                context.x(),
                context.y(),
                0,
                frame * contents.width(),
                0.0F,
                contents.width(),
                contents.height(),
                contents.width() * contents.frameCount(),
                contents.height());
    }

    public static final class Registry {
        private final Map<ResourceLocation, Entry<?>> renderers = new LinkedHashMap<>();

        public Registry() {
        }

        private Registry(Registry source) {
            renderers.putAll(source.renderers);
        }

        public Registry copy() {
            return new Registry(this);
        }

        public <T extends PiVisualTextContents> Registry register(
                ResourceLocation type,
                Class<T> contentsType,
                PiVisualTextRenderer<? super T> renderer
        ) {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(contentsType, "contentsType");
            Objects.requireNonNull(renderer, "renderer");
            Entry<?> previous = renderers.putIfAbsent(type, new Entry<>(contentsType, renderer));
            if (previous != null) {
                throw new IllegalArgumentException("duplicate visual text renderer: " + type);
            }
            return this;
        }

        public boolean has(ResourceLocation type) {
            return renderers.containsKey(Objects.requireNonNull(type, "type"));
        }

        public Optional<PiVisualTextRenderer<?>> renderer(ResourceLocation type) {
            Entry<?> entry = renderers.get(Objects.requireNonNull(type, "type"));
            return entry == null ? Optional.empty() : Optional.of(entry.renderer());
        }

        public int width(Font font, Component component) {
            Objects.requireNonNull(font, "font");
            Objects.requireNonNull(component, "component");
            return widthNode(font, component);
        }

        public int drawLine(
                GuiGraphics graphics,
                Font font,
                Component component,
                int x,
                int y,
                int color,
                boolean shadow,
                long gameTime
        ) {
            Objects.requireNonNull(graphics, "graphics");
            Objects.requireNonNull(font, "font");
            Objects.requireNonNull(component, "component");
            return drawNode(graphics, font, component, x, y, color, shadow, gameTime);
        }

        private int widthNode(Font font, Component component) {
            int width = ownWidth(font, component);
            for (Component sibling : component.getSiblings()) {
                width += widthNode(font, sibling);
            }
            return width;
        }

        private int ownWidth(Font font, Component component) {
            ComponentContents contents = component.getContents();
            if (contents == ComponentContents.EMPTY) {
                return 0;
            }
            if (contents instanceof PiVisualTextContents visual) {
                return visual.width();
            }
            return font.width(contentsOnly(component));
        }

        private int drawNode(
                GuiGraphics graphics,
                Font font,
                Component component,
                int x,
                int y,
                int color,
                boolean shadow,
                long gameTime
        ) {
            int cursor = drawOwn(graphics, font, component, x, y, color, shadow, gameTime);
            for (Component sibling : component.getSiblings()) {
                cursor = drawNode(graphics, font, sibling, cursor, y, color, shadow, gameTime);
            }
            return cursor;
        }

        private int drawOwn(
                GuiGraphics graphics,
                Font font,
                Component component,
                int x,
                int y,
                int color,
                boolean shadow,
                long gameTime
        ) {
            ComponentContents contents = component.getContents();
            if (contents == ComponentContents.EMPTY) {
                return x;
            }
            if (contents instanceof PiVisualTextContents visual) {
                Entry<?> entry = renderers.get(visual.type());
                if (entry == null) {
                    MutableComponent fallback = visual.fallback().copy().withStyle(component.getStyle());
                    graphics.drawString(font, fallback, x, y, color, shadow);
                    return x + font.width(fallback);
                }
                int visualY = y + (font.lineHeight - visual.height()) / 2;
                entry.render(new PiVisualTextRenderContext(graphics, font, x, visualY, color, shadow, gameTime), visual);
                return x + visual.width();
            }
            MutableComponent vanilla = contentsOnly(component);
            graphics.drawString(font, vanilla, x, y, color, shadow);
            return x + font.width(vanilla);
        }

        private static MutableComponent contentsOnly(Component component) {
            ComponentContents contents = component.getContents();
            if (contents == ComponentContents.EMPTY) {
                return Component.empty().withStyle(component.getStyle());
            }
            try {
                return MutableComponent.create(contents).withStyle(component.getStyle());
            } catch (RuntimeException ignored) {
                return PiTexts.literal(component.getString()).withStyle(component.getStyle());
            }
        }
    }

    private record Entry<T extends PiVisualTextContents>(
            Class<T> contentsType,
            PiVisualTextRenderer<? super T> renderer
    ) {
        private Entry {
            Objects.requireNonNull(contentsType, "contentsType");
            Objects.requireNonNull(renderer, "renderer");
        }

        private void render(PiVisualTextRenderContext context, PiVisualTextContents contents) {
            if (!contentsType.isInstance(contents)) {
                throw new IllegalArgumentException(
                        "visual text content " + contents.type() + " is "
                                + contents.getClass().getName() + ", expected " + contentsType.getName());
            }
            renderer.render(context, contentsType.cast(contents));
        }
    }
}
