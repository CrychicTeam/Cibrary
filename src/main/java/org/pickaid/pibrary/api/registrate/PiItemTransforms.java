package org.pickaid.pibrary.api.registrate;

import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Item-side transforms for Registrate chains.
 *
 * <p>The point is not to replace {@link ItemBuilder}. It is to make the common
 * parts read like a named recipe: model, tags, stack size, rarity, and tab.</p>
 */
public final class PiItemTransforms {
    private PiItemTransforms() {
    }

    public static <T extends Item, P> NonNullFunction<ItemBuilder<T, P>, ItemBuilder<T, P>> defaultModel() {
        return ItemBuilder::defaultModel;
    }

    public static <T extends Item, P> NonNullFunction<ItemBuilder<T, P>, ItemBuilder<T, P>> defaultLang() {
        return ItemBuilder::defaultLang;
    }

    public static <T extends Item, P> NonNullFunction<ItemBuilder<T, P>, ItemBuilder<T, P>>
    properties(NonNullUnaryOperator<Item.Properties> properties) {
        Objects.requireNonNull(properties, "properties");
        return builder -> builder.properties(properties);
    }

    public static <T extends Item, P> NonNullFunction<ItemBuilder<T, P>, ItemBuilder<T, P>> stacksTo(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("stack size must be positive");
        }
        return builder -> builder.properties(properties -> properties.stacksTo(count));
    }

    public static <T extends Item, P> NonNullFunction<ItemBuilder<T, P>, ItemBuilder<T, P>> durability(int durability) {
        if (durability <= 0) {
            throw new IllegalArgumentException("durability must be positive");
        }
        return builder -> builder.properties(properties -> properties.durability(durability));
    }

    public static <T extends Item, P> NonNullFunction<ItemBuilder<T, P>, ItemBuilder<T, P>> fireResistant() {
        return builder -> builder.properties(Item.Properties::fireResistant);
    }

    public static <T extends Item, P> NonNullFunction<ItemBuilder<T, P>, ItemBuilder<T, P>> rarity(Rarity rarity) {
        Objects.requireNonNull(rarity, "rarity");
        return builder -> builder.properties(properties -> properties.rarity(rarity));
    }

    public static <T extends Item, P> NonNullFunction<ItemBuilder<T, P>, ItemBuilder<T, P>>
    tab(ResourceKey<CreativeModeTab> tab) {
        Objects.requireNonNull(tab, "tab");
        return builder -> builder.tab(tab);
    }

    @SafeVarargs
    public static <T extends Item, P> NonNullFunction<ItemBuilder<T, P>, ItemBuilder<T, P>>
    tag(TagKey<Item>... tags) {
        TagKey<Item>[] copy = copyTags(tags);
        return builder -> builder.tag(copy);
    }

    private static TagKey<Item>[] copyTags(TagKey<Item>[] tags) {
        Objects.requireNonNull(tags, "tags");
        TagKey<Item>[] copy = Arrays.copyOf(tags, tags.length);
        for (int i = 0; i < copy.length; i++) {
            Objects.requireNonNull(copy[i], "tags[" + i + "]");
        }
        return copy;
    }
}
