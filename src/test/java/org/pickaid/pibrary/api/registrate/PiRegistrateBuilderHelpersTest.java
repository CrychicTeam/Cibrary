package org.pickaid.pibrary.api.registrate;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import org.junit.jupiter.api.Test;

class PiRegistrateBuilderHelpersTest {
    @Test
    void blockHelpersKeepThePibraryBuilderChain() {
        assertBlockBuilderApi((PiBlockBuilder<Block, ?>) null);
    }

    @Test
    void itemHelpersKeepThePibraryBuilderChain() {
        assertItemBuilderApi((PiItemBuilder<Item, ?>) null);
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
}
