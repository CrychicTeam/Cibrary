package org.pickaid.pibrary.api.text.component;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for non-vanilla visual content inside a Minecraft text tree.
 *
 * <p>Vanilla {@link Component} is not just a string: it is a tree whose leaf
 * behaviour lives in {@link ComponentContents}. Visual text nodes belong there,
 * otherwise callers lose style traversal, fallback text, and the ability to
 * inspect content through {@link Component#getContents()}.</p>
 *
 * <p>Important limitation: vanilla's JSON serializer only knows vanilla
 * content types. Use {@link PiTextComponents#vanillaFallback(Component)} before
 * writing custom visual contents to JSON or sending them through a path that
 * expects vanilla component serialization.</p>
 */
public abstract class PiVisualTextContents implements ComponentContents {
    private final ResourceLocation type;
    private final Component fallback;

    protected PiVisualTextContents(ResourceLocation type, Component fallback) {
        this.type = Objects.requireNonNull(type, "type");
        this.fallback = Objects.requireNonNull(fallback, "fallback").copy();
    }

    public ResourceLocation type() {
        return type;
    }

    public final Component fallback() {
        return fallback.copy();
    }

    public final MutableComponent component() {
        return MutableComponent.create(this);
    }

    public abstract int width();

    public abstract int height();

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return fallback.visit(styledContentConsumer, style);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        return fallback.visit(contentConsumer);
    }

    @Override
    public MutableComponent resolve(
            @Nullable CommandSourceStack commandSource,
            @Nullable Entity entity,
            int recursionDepth
    ) {
        return component();
    }

    protected final boolean baseEquals(PiVisualTextContents other) {
        return type.equals(other.type) && fallback.equals(other.fallback);
    }

    protected final int baseHashCode() {
        return Objects.hash(type, fallback);
    }
}
