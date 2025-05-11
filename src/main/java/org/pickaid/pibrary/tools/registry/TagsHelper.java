package org.pickaid.pibrary.tools.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class TagsHelper {
    public static boolean isItemEqual(ItemStack stack, ResourceLocation rLoc) {
        if (ForgeRegistries.ITEMS.containsKey(rLoc)) {
            return ForgeRegistries.ITEMS.getValue(rLoc) == stack.getItem();
        } else {
            return isItemIn(stack.getItem(), rLoc);
        }
    }

    public static List<Item> getItemTagContents(ResourceLocation tagID) {
        try {
            ITag<Item> tag = Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).getTag(Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).createTagKey(tagID));
            return tag.stream().collect(Collectors.toList());
        } catch (Exception ignored) {}
        return new ArrayList<Item>();
    }

    public static List<Biome> getBiomeTagContents(ServerLevel world, ResourceLocation tagID) {
        try {
            Registry<Biome> biomeRegistry = world.registryAccess().registryOrThrow(Registries.BIOME);
            TagKey<Biome> tag = TagKey.create(Registries.BIOME, tagID);
            Optional<HolderSet.Named<Biome>> biomeTag = biomeRegistry.getTag(tag);
            if (biomeTag.isPresent()) {
                return (List<Biome>)((HolderSet.Named)biomeTag.get()).stream().collect(Collectors.toList());
            }
        } catch (Exception ignored) {}

        return new ArrayList<Biome>();
    }

    public static List<Block> getBlockTagContents(ResourceLocation tagID) {
        try {
            ITag<Block> tag = Objects.requireNonNull(ForgeRegistries.BLOCKS.tags()).getTag(Objects.requireNonNull(ForgeRegistries.BLOCKS.tags()).createTagKey(tagID));
            return tag.stream().collect(Collectors.toList());
        } catch (Exception ignored) {}
        return new ArrayList<Block>();
    }

    public static List<Holder<Structure>> getStructureContents(ServerLevel world, ResourceLocation tagID) {
        try {
            Registry<Structure> registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
            TagKey<Structure> tagKey = TagKey.create(registry.key(), tagID);
            Optional<HolderSet.Named<Structure>> tag = registry.getTag(tagKey);
            if (tag.isPresent()) {
                return (List)((HolderSet.Named)tag.get()).stream().collect(Collectors.toList());
            }
        } catch (Exception ignored) {}
        return new ArrayList<Holder<Structure>>();
    }

    @Nullable
    public static Structure getStructure(ServerLevel world, ResourceLocation tagID) {
        try {
            Registry<Structure> registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
            if (registry.containsKey(tagID)) {
                return registry.get(tagID);
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static List<Item> smartLookupItem(ResourceLocation rLoc) {
        if (rLoc == null) {
            return new ArrayList<Item>();
        } else {
            return ForgeRegistries.ITEMS.containsKey(rLoc) ? Collections.singletonList(ForgeRegistries.ITEMS.getValue(rLoc)) : getItemTagContents(rLoc);
        }
    }

    public static List<Block> smartLookupBlock(ResourceLocation rLoc) {
        if (rLoc == null) {
            return new ArrayList<Block>();
        } else {
            return ForgeRegistries.BLOCKS.containsKey(rLoc) ? Collections.singletonList(ForgeRegistries.BLOCKS.getValue(rLoc)) : getBlockTagContents(rLoc);
        }
    }

    public static ItemStack lookupItem(ResourceLocation rLoc) {
        return ForgeRegistries.ITEMS.containsKey(rLoc) ? new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(rLoc))) : ItemStack.EMPTY;
    }

    public static boolean isBlockIn(Block block, ResourceLocation tag) {
        try {
            return getBlockTagContents(tag).contains(block);
        } catch (Exception var3) {
            return false;
        }
    }

    public static boolean isItemIn(Item item, ResourceLocation tag) {
        try {
            return getItemTagContents(tag).contains(item);
        } catch (Exception var3) {
            return false;
        }
    }

    public static boolean isStructureIn(ServerLevel world, Holder<Structure> structure, ResourceLocation tag) {
        try {
            TagKey<Structure> key = TagKey.create(Registries.STRUCTURE, tag);
            world.registryAccess().registryOrThrow(Registries.STRUCTURE).getOrCreateTag(key).contains(structure);
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean doesEntityTagExist(ResourceLocation tagID) {
        try {
            ITag<EntityType<?>> tag = Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.tags()).getTag(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.tags()).createTagKey(tagID));
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    public static List<EntityType<?>> getEntitiesOnTag(ResourceLocation tagID) {
        try {
            ITag<EntityType<?>> tag = Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.tags()).getTag(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.tags()).createTagKey(tagID));
            return tag.stream().collect(Collectors.toList());
        } catch (Exception ignored) {}
        return new ArrayList<EntityType<?>>();
    }
}