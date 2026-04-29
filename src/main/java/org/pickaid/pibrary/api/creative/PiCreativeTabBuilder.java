package org.pickaid.pibrary.api.creative;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Small wrapper around Minecraft's creative-tab builder.
 */
public final class PiCreativeTabBuilder {
    private final ResourceKey<CreativeModeTab> tab;
    private final CreativeModeTab.Builder builder;
    private final PiCreativeContentRegistry contents;

    public PiCreativeTabBuilder(
            ResourceKey<CreativeModeTab> tab,
            CreativeModeTab.Builder builder,
            PiCreativeContentRegistry contents
    ) {
        this.tab = Objects.requireNonNull(tab, "tab");
        this.builder = Objects.requireNonNull(builder, "builder");
        this.contents = Objects.requireNonNull(contents, "contents");
    }

    public PiCreativeTabBuilder title(String translationKey) {
        return title(Component.translatable(Objects.requireNonNull(translationKey, "translationKey")));
    }

    public PiCreativeTabBuilder title(Component title) {
        builder.title(Objects.requireNonNull(title, "title"));
        return this;
    }

    public PiCreativeTabBuilder icon(Supplier<? extends ItemLike> item) {
        Objects.requireNonNull(item, "item");
        return iconStack(() -> new ItemStack(Objects.requireNonNull(item.get(), "item supplier returned null")));
    }

    public PiCreativeTabBuilder icon(ItemLike item) {
        Objects.requireNonNull(item, "item");
        return iconStack(() -> new ItemStack(item));
    }

    public PiCreativeTabBuilder iconStack(Supplier<ItemStack> stack) {
        builder.icon(Objects.requireNonNull(stack, "stack"));
        return this;
    }

    public PiCreativeTabBuilder sections(String... sections) {
        contents.order(tab, sections);
        return this;
    }

    public PiCreativeTabBuilder alignedRight() {
        builder.alignedRight();
        return this;
    }

    public PiCreativeTabBuilder hideTitle() {
        builder.hideTitle();
        return this;
    }

    public PiCreativeTabBuilder noScrollBar() {
        builder.noScrollBar();
        return this;
    }

    public PiCreativeTabBuilder backgroundSuffix(String suffix) {
        builder.backgroundSuffix(Objects.requireNonNull(suffix, "suffix"));
        return this;
    }

    public PiCreativeTabBuilder configureRaw(Consumer<CreativeModeTab.Builder> configure) {
        Objects.requireNonNull(configure, "configure").accept(builder);
        return this;
    }
}
