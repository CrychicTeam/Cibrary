package org.pickaid.pibrary.tools.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class RegistryHelper {
    public static ResourceLocation getItem(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }

    public static ResourceLocation getItem(ItemStack stack) {
        return getItem(stack.getItem());
    }

    public static ResourceLocation getBlock(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    public static ResourceLocation getBlock(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem item) return getBlock(item.getBlock());
        return null;
    }

    public static ResourceLocation getBlockItem(Block block) {
        return ForgeRegistries.ITEMS.getKey(block.asItem());
    }
}
