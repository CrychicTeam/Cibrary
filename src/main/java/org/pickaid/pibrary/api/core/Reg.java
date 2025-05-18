package org.pickaid.pibrary.api.core;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class Reg{
    private String mod_id;

    /**
     * Creates a new Reg instance for the specified mod ID.
     *
     * @param modId The mod ID to use for all registries created by this instance
     */
    public Reg(String modId) {
        this.mod_id = modId;
    }

    public static Reg of(String modId) {
        return new Reg(modId);
    }

    public <T> ResourceKey<Registry<T>> create(String key) {
        return ResourceKey.createRegistryKey(new ResourceLocation(this.mod_id, key));
    }

    /**
     * Creates a DeferredRegister for a registry specified by its ResourceKey.
     *
     * @param <T> The type of objects in the registry
     * @param key The ResourceKey for the registry
     * @return A new DeferredRegister for the specified registry
     */
    public <T> DeferredRegister<T> make(ResourceKey<? extends Registry<T>> key) {
        return DeferredRegister.create(key, mod_id);
    }

    /**
     * Creates a DeferredRegister for a Forge registry.
     *
     * @param <T> The type of objects in the registry
     * @param registry The Forge registry to create a DeferredRegister for
     * @return A new DeferredRegister for the specified registry
     */
    public <T> DeferredRegister<T> make(IForgeRegistry<T> registry) {
        return DeferredRegister.create(registry, mod_id);
    }

    /**
     * Creates a DeferredRegister for items.
     *
     * @return A new DeferredRegister for items
     */
    public DeferredRegister<Item> item() {
        return DeferredRegister.create(ForgeRegistries.ITEMS, mod_id);
    }

    /**
     * Creates a DeferredRegister for blocks.
     *
     * @return A new DeferredRegister for blocks
     */
    public DeferredRegister<Block> block() {
        return DeferredRegister.create(ForgeRegistries.BLOCKS, mod_id);
    }

    /**
     * Creates a DeferredRegister for recipe serializers.
     *
     * @return A new DeferredRegister for recipe serializers
     */
    public DeferredRegister<RecipeSerializer<?>> recipeSerializer() {
        return DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, mod_id);
    }

    /**
     * Creates a DeferredRegister for block entities.
     *
     * @return A new DeferredRegister for block entities
     */
    public DeferredRegister<BlockEntityType<?>> blockEntity() {
        return DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, mod_id);
    }

    /**
     * Creates a DeferredRegister for attributes.
     *
     * @return A new DeferredRegister for attributes
     */
    public DeferredRegister<Attribute> attribute() {
        return DeferredRegister.create(ForgeRegistries.ATTRIBUTES, mod_id);
    }

    /**
     * Creates a DeferredRegister for entity types.
     *
     * @return A new DeferredRegister for entity types
     */
    public DeferredRegister<EntityType<?>> entity() {
        return DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, mod_id);
    }

    /**
     * Creates a DeferredRegister for recipe types.
     *
     * @return A new DeferredRegister for recipe types
     */
    public DeferredRegister<RecipeType<?>> recipeType() {
        return DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, mod_id);
    }

    /**
     * Creates a DeferredRegister for mob effects.
     *
     * @return A new DeferredRegister for mob effects
     */
    public DeferredRegister<MobEffect> mobEffect() {
        return DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, mod_id);
    }

    /**
     * Creates a DeferredRegister for sound events.
     *
     * @return A new DeferredRegister for sound events
     */
    public DeferredRegister<SoundEvent> soundEvent() {
        return DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, mod_id);
    }

    /**
     * Creates a DeferredRegister for potions.
     *
     * @return A new DeferredRegister for potions
     */
    public DeferredRegister<Potion> potion() {
        return DeferredRegister.create(ForgeRegistries.POTIONS, mod_id);
    }

    /**
     * Creates a DeferredRegister for enchantments.
     *
     * @return A new DeferredRegister for enchantments
     */
    public DeferredRegister<Enchantment> enchantment() {
        return DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, mod_id);
    }

    /**
     * Creates a DeferredRegister for particle types.
     *
     * @return A new DeferredRegister for particle types
     */
    public DeferredRegister<ParticleType<?>> particleType() {
        return DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, mod_id);
    }

    /**
     * Creates a DeferredRegister for menu types.
     *
     * @return A new DeferredRegister for menu types
     */
    public DeferredRegister<MenuType<?>> menuType() {
        return DeferredRegister.create(ForgeRegistries.MENU_TYPES, mod_id);
    }

    /**
     * Creates a DeferredRegister for painting variants.
     *
     * @return A new DeferredRegister for painting variants
     */
    public DeferredRegister<PaintingVariant> paintingVariant() {
        return DeferredRegister.create(ForgeRegistries.PAINTING_VARIANTS, mod_id);
    }

    /**
     * Creates a DeferredRegister for fluids.
     *
     * @return A new DeferredRegister for fluids
     */
    public DeferredRegister<Fluid> fluid() {
        return DeferredRegister.create(ForgeRegistries.FLUIDS, mod_id);
    }

    /**
     * Creates a DeferredRegister for fluid types.
     *
     * @return A new DeferredRegister for fluid types
     */
    public DeferredRegister<FluidType> fluidType() {
        return DeferredRegister.create(ForgeRegistries.FLUID_TYPES.get(), mod_id);
    }

    /**
     * Creates a DeferredRegister for villager professions.
     *
     * @return A new DeferredRegister for villager professions
     */
    public DeferredRegister<VillagerProfession> villagerProfession() {
        return DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, mod_id);
    }

    /**
     * Creates a DeferredRegister for POI types.
     *
     * @return A new DeferredRegister for POI types
     */
    public DeferredRegister<PoiType> poiType() {
        return DeferredRegister.create(ForgeRegistries.POI_TYPES, mod_id);
    }

    /**
     * Creates a DeferredRegister for memory module types.
     *
     * @return A new DeferredRegister for memory module types
     */
    public DeferredRegister<MemoryModuleType<?>> memoryModuleType() {
        return DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, mod_id);
    }

    /**
     * Creates a DeferredRegister for sensor types.
     *
     * @return A new DeferredRegister for sensor types
     */
    public DeferredRegister<SensorType<?>> sensorType() {
        return DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, mod_id);
    }

    /**
     * Creates a DeferredRegister for schedules.
     *
     * @return A new DeferredRegister for schedules
     */
    public DeferredRegister<Schedule> schedule() {
        return DeferredRegister.create(ForgeRegistries.SCHEDULES, mod_id);
    }

    /**
     * Creates a DeferredRegister for activities.
     *
     * @return A new DeferredRegister for activities
     */
    public DeferredRegister<Activity> activity() {
        return DeferredRegister.create(ForgeRegistries.ACTIVITIES, mod_id);
    }

    /**
     * Creates a DeferredRegister for world carvers.
     *
     * @return A new DeferredRegister for world carvers
     */
    public DeferredRegister<WorldCarver<?>> worldCarver() {
        return DeferredRegister.create(ForgeRegistries.WORLD_CARVERS, mod_id);
    }

    /**
     * Creates a DeferredRegister for features.
     *
     * @return A new DeferredRegister for features
     */
    public DeferredRegister<Feature<?>> feature() {
        return DeferredRegister.create(ForgeRegistries.FEATURES, mod_id);
    }

    /**
     * Creates a DeferredRegister for chunk statuses.
     *
     * @return A new DeferredRegister for chunk statuses
     */
    public DeferredRegister<ChunkStatus> chunkStatus() {
        return DeferredRegister.create(ForgeRegistries.CHUNK_STATUS, mod_id);
    }

    /**
     * Creates a DeferredRegister for biomes.
     *
     * @return A new DeferredRegister for biomes
     */
    public DeferredRegister<Biome> biome() {
        return DeferredRegister.create(ForgeRegistries.BIOMES, mod_id);
    }

    /**
     * Creates a DeferredRegister for biome modifier serializers.
     *
     * @return A new DeferredRegister for biome modifier serializers
     */
    public DeferredRegister<Codec<? extends BiomeModifier>> biomeModifierSerializer() {
        return DeferredRegister.create(ForgeRegistries.BIOME_MODIFIER_SERIALIZERS.get(), mod_id);
    }

    /**
     * Creates a DeferredRegister for global loot modifier serializers.
     *
     * @return A new DeferredRegister for global loot modifier serializers
     */
    public DeferredRegister<Codec<? extends IGlobalLootModifier>> globalLootModifierSerializer() {
        return DeferredRegister.create(ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get(), mod_id);
    }
}