package org.pickaid.pibrary.api.registrate;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Small transforms that plug into Registrate's {@code transform(...)} chain.
 *
 * <p>These methods keep common chains short without hiding Registrate itself.
 * Use them for repeated, boring parts such as mining tags and simple block
 * item tags. Keep project-specific model or behaviour wiring in your own mod
 * code.</p>
 */
public final class PiBlockTransforms {
    private PiBlockTransforms() {
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> pickaxeOnly() {
        return tag(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOnly() {
        return tag(BlockTags.MINEABLE_WITH_AXE);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> shovelOnly() {
        return tag(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> hoeOnly() {
        return tag(BlockTags.MINEABLE_WITH_HOE);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOrPickaxe() {
        return tag(BlockTags.MINEABLE_WITH_AXE, BlockTags.MINEABLE_WITH_PICKAXE);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> needsStoneTool() {
        return tag(BlockTags.NEEDS_STONE_TOOL);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> needsIronTool() {
        return tag(BlockTags.NEEDS_IRON_TOOL);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> needsDiamondTool() {
        return tag(BlockTags.NEEDS_DIAMOND_TOOL);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> requiresTool() {
        return builder -> builder.properties(BlockBehaviour.Properties::requiresCorrectToolForDrops);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> noOcclusion() {
        return builder -> builder.properties(BlockBehaviour.Properties::noOcclusion);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> noMobSpawn() {
        return builder -> builder.properties(properties -> properties
                .isValidSpawn((state, level, pos, type) -> false));
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> lightLevel(int value) {
        if (value < 0 || value > 15) {
            throw new IllegalArgumentException("light level must be inside [0, 15]");
        }
        return builder -> builder.properties(properties -> properties.lightLevel(state -> value));
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>>
    machine(float destroyTime, float explosionResistance) {
        return builder -> builder.properties(properties -> properties
                .strength(destroyTime, explosionResistance)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> machine() {
        return machine(4.0F, 8.0F);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> softMachine() {
        return builder -> builder.properties(properties -> properties
                .strength(2.0F, 6.0F)
                .requiresCorrectToolForDrops());
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> defaultBlockstateAndLoot() {
        return builder -> builder.defaultBlockstate().defaultLoot();
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> simpleItem() {
        return BlockBuilder::simpleItem;
    }

    @SafeVarargs
    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>>
    simpleItemWithTags(TagKey<Item>... itemTags) {
        TagKey<Item>[] copy = copyTags(itemTags, "itemTags");
        return builder -> {
            builder.item().tag(copy).build();
            return builder;
        };
    }

    public static <T extends Block, P, BE extends BlockEntity> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>>
    simpleBlockEntity(BlockEntityBuilder.BlockEntityFactory<BE> factory) {
        Objects.requireNonNull(factory, "factory");
        return builder -> builder.simpleBlockEntity(factory);
    }

    @SafeVarargs
    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>>
    tag(TagKey<Block>... tags) {
        Objects.requireNonNull(tags, "tags");
        return builder -> {
            BlockBuilder<T, P> result = builder;
            for (TagKey<Block> tag : tags) {
                result = result.tag(Objects.requireNonNull(tag, "tag"));
            }
            return result;
        };
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BlockItem, BlockBuilder<T, P>>>
    tagBlockAndItem(TagKey<Block> blockTag, TagKey<Item> itemTag) {
        return tagBlockAndItem(Map.of(blockTag, itemTag));
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BlockItem, BlockBuilder<T, P>>>
    tagBlockAndItem(Map<TagKey<Block>, TagKey<Item>> tags) {
        Objects.requireNonNull(tags, "tags");
        return builder -> {
            for (TagKey<Block> blockTag : tags.keySet()) {
                builder.tag(Objects.requireNonNull(blockTag, "blockTag"));
            }
            ItemBuilder<BlockItem, BlockBuilder<T, P>> item = builder.item();
            for (TagKey<Item> itemTag : tags.values()) {
                item.tag(Objects.requireNonNull(itemTag, "itemTag"));
            }
            return item;
        };
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>>
    tagBlockAndSimpleItem(TagKey<Block> blockTag, TagKey<Item> itemTag) {
        return tagBlockAndSimpleItem(Map.of(blockTag, itemTag));
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>>
    tagBlockAndSimpleItem(Map<TagKey<Block>, TagKey<Item>> tags) {
        Objects.requireNonNull(tags, "tags");
        return builder -> {
            for (TagKey<Block> blockTag : tags.keySet()) {
                builder.tag(Objects.requireNonNull(blockTag, "blockTag"));
            }
            ItemBuilder<BlockItem, BlockBuilder<T, P>> item = builder.item();
            for (TagKey<Item> itemTag : tags.values()) {
                item.tag(Objects.requireNonNull(itemTag, "itemTag"));
            }
            item.build();
            return builder;
        };
    }

    private static TagKey<Item>[] copyTags(TagKey<Item>[] tags, String name) {
        Objects.requireNonNull(tags, name);
        TagKey<Item>[] copy = Arrays.copyOf(tags, tags.length);
        for (int i = 0; i < copy.length; i++) {
            Objects.requireNonNull(copy[i], name + "[" + i + "]");
        }
        return copy;
    }
}
