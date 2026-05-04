package org.pickaid.pibrary.api.registrate;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.config.PiDataConfigType;

class PiRegistrateBuilderHelpersTest {
    @Test
    void blockHelpersKeepThePibraryBuilderChain() {
        assertBlockBuilderApi((PiBlockBuilder<Block, ?>) null);
    }

    @Test
    void itemHelpersKeepThePibraryBuilderChain() {
        assertItemBuilderApi((PiItemBuilder<Item, ?>) null);
    }

    @Test
    void registrateCanKeepDataConfigEntries() {
        assertDataConfigEntryApi((PiBaseRegistrate<?>) null);
    }

    private static void assertBlockBuilderApi(PiBlockBuilder<Block, ?> builder) {
        if (builder == null) {
            return;
        }
        builder
                .initialProperties(PiBlockProps::metal)
                .blockAndItemTag(Tags.Blocks.STORAGE_BLOCKS_IRON, Tags.Items.STORAGE_BLOCKS_IRON)
                .blockTag(BlockTags.MINEABLE_WITH_PICKAXE)
                .blockTags(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
                .itemTag(Tags.Items.STORAGE_BLOCKS_IRON)
                .itemTags(Tags.Items.STORAGE_BLOCKS_IRON)
                .requiresTool()
                .noOcclusion()
                .noMobSpawn()
                .lightLevel(8)
                .simpleItem()
                .section("machines")
                .variant("charged", stack -> stack.getOrCreateTag().putBoolean("charged", true));
    }

    private static void assertItemBuilderApi(PiItemBuilder<Item, ?> builder) {
        if (builder == null) {
            return;
        }
        builder
                .itemTag(Tags.Items.INGOTS_COPPER)
                .itemTags(Tags.Items.INGOTS_COPPER)
                .stacksTo(1)
                .durability(250)
                .fireResistant()
                .rarity(net.minecraft.world.item.Rarity.RARE)
                .section("items")
                .variant("charged", stack -> stack.getOrCreateTag().putBoolean("charged", true));
    }

    private static void assertDataConfigEntryApi(PiBaseRegistrate<?> registrate) {
        if (registrate == null) {
            return;
        }
        PiDataConfigType<String> spells = PiDataConfigType.create("spell", Codec.STRING);
        registrate.dataConfig(spells.entry(id("fireball"), "enabled"));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("example", path);
    }
}
