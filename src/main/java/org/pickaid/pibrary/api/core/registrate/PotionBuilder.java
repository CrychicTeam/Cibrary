package org.pickaid.pibrary.api.core.registrate;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.registries.RegistryObject;
import org.pickaid.pibrary.api.core.Reg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for registering potions and brewing recipes
 */
public class PotionBuilder {
    private final String modId;
    private final List<Runnable> recipeRegistrations = new ArrayList<>();

    /**
     * Creates a new PotionBuilder for the specified mod ID
     *
     * @param modId The mod ID to use for registry names
     */
    public PotionBuilder(String modId) {
        this.modId = modId;
    }

    public static PotionBuilder of(String modId) {
        return new PotionBuilder(modId);
    }

    /**
     * Registers all pending brewing recipes
     * Should be called during FMLCommonSetupEvent
     */
    public void registerBrewingRecipes() {
        recipeRegistrations.forEach(Runnable::run);
    }

    // ===== Basic Registration Methods =====

    /**
     * Registers a basic potion with a single effect
     *
     * @param id Registry ID suffix
     * @param effect The effect to apply
     * @param duration Effect duration in ticks
     * @param amplifier Effect amplifier
     * @return The registered potion
     */
    public RegistryObject<Potion> registerPotion(String id, MobEffect effect, int duration, int amplifier) {
        return Reg.of(modId).potion().register(id, () ->
                new Potion(new MobEffectInstance(effect, duration, amplifier))
        );
    }

    /**
     * Registers a potion with multiple effects
     *
     * @param id Registry ID suffix
     * @param effects List of effects with their durations and amplifiers
     * @return The registered potion
     */
    public RegistryObject<Potion> registerMultiEffectPotion(String id, List<MobEffectInstance> effects) {
        return Reg.of(modId).potion().register(id, () ->
                new Potion(effects.toArray(new MobEffectInstance[0]))
        );
    }

    /**
     * Registers a potion with multiple effects
     *
     * @param id Registry ID suffix
     * @param effects Array of effects with their durations and amplifiers
     * @return The registered potion
     */
    public RegistryObject<Potion> registerMultiEffectPotion(String id, MobEffectInstance... effects) {
        return registerMultiEffectPotion(id, Arrays.asList(effects));
    }

    // ===== Brewing Recipe Methods =====

    /**
     * Adds a brewing recipe
     *
     * @param source The source potion
     * @param item The ingredient item
     * @param result The result potion
     */
    public void addMix(Potion source, ItemLike item, Potion result) {
        ItemStack resultStack = PotionUtils.setPotion(new ItemStack(Items.POTION), result);
        recipeRegistrations.add(() ->
                BrewingRecipeRegistry.addRecipe(
                        Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), source)),
                        Ingredient.of(item.asItem()),
                        resultStack
                )
        );
    }

    /**
     * Registers a potion with a brewing recipe
     *
     * @param id Registry ID suffix
     * @param effect The effect to apply
     * @param source The source potion
     * @param ingredient The brewing ingredient
     * @param duration Effect duration in ticks
     * @param amplifier Effect amplifier
     * @return The registered potion
     */
    public RegistryObject<Potion> registerPotionWithRecipe(String id, MobEffect effect, Potion source,
                                                           ItemLike ingredient, int duration, int amplifier) {
        RegistryObject<Potion> potion = registerPotion(id, effect, duration, amplifier);
        addMix(source, ingredient, potion.get());
        return potion;
    }

    /**
     * Registers a potion with multiple effects and a brewing recipe
     *
     * @param id Registry ID suffix
     * @param effects List of effects with their durations and amplifiers
     * @param source The source potion
     * @param ingredient The brewing ingredient
     * @return The registered potion
     */
    public RegistryObject<Potion> registerMultiEffectPotionWithRecipe(String id, List<MobEffectInstance> effects,
                                                                      Potion source, ItemLike ingredient) {
        RegistryObject<Potion> potion = registerMultiEffectPotion(id, effects);
        addMix(source, ingredient, potion.get());
        return potion;
    }

    /**
     * Registers a potion with multiple effects and a brewing recipe
     *
     * @param id Registry ID suffix
     * @param source The source potion
     * @param ingredient The brewing ingredient
     * @param effects Array of effects with their durations and amplifiers
     * @return The registered potion
     */
    public RegistryObject<Potion> registerMultiEffectPotionWithRecipe(String id, Potion source,
                                                                      ItemLike ingredient, MobEffectInstance... effects) {
        return registerMultiEffectPotionWithRecipe(id, Arrays.asList(effects), source, ingredient);
    }

    // ===== Variant Registration Methods =====

    /**
     * Registers a standard potion with its long variant
     *
     * @param id Registry ID suffix
     * @param effect The effect to apply
     * @param ingredient The brewing ingredient
     * @param duration Normal duration in ticks
     * @param longDuration Extended duration in ticks
     * @return The base potion registry object
     */
    public RegistryObject<Potion> registerPotionWithLong(String id, MobEffect effect, ItemLike ingredient,
                                                         int duration, int longDuration) {
        RegistryObject<Potion> potion = registerPotionWithRecipe(id, effect, Potions.AWKWARD, ingredient, duration, 0);
        registerPotionWithRecipe("long_" + id, effect, potion.get(), Items.REDSTONE, longDuration, 0);
        return potion;
    }

    /**
     * Registers a standard potion with its long and strong variants
     *
     * @param id Registry ID suffix
     * @param effect The effect to apply
     * @param ingredient The brewing ingredient
     * @param duration Normal duration in ticks
     * @param longDuration Extended duration in ticks
     * @param strongDuration Strong duration in ticks
     * @param amplifier Normal amplifier
     * @param strongAmplifier Strong amplifier
     * @return The base potion registry object
     */
    public RegistryObject<Potion> registerPotionWithVariants(String id, MobEffect effect, ItemLike ingredient,
                                                             int duration, int longDuration, int strongDuration,
                                                             int amplifier, int strongAmplifier) {
        RegistryObject<Potion> potion = registerPotionWithRecipe(id, effect, Potions.AWKWARD, ingredient, duration, amplifier);
        registerPotionWithRecipe("long_" + id, effect, potion.get(), Items.REDSTONE, longDuration, amplifier);
        registerPotionWithRecipe("strong_" + id, effect, potion.get(), Items.GLOWSTONE_DUST, strongDuration, strongAmplifier);
        return potion;
    }

    /**
     * Registers a standard potion with its long and strong variants using custom ingredients
     *
     * @param id Registry ID suffix
     * @param effect The effect to apply
     * @param ingredient The brewing ingredient
     * @param duration Normal duration in ticks
     * @param longDuration Extended duration in ticks
     * @param strongDuration Strong duration in ticks
     * @param amplifier Normal amplifier
     * @param strongAmplifier Strong amplifier
     * @param longIngredient The ingredient for long variant
     * @param strongIngredient The ingredient for strong variant
     * @return The base potion registry object
     */
    public RegistryObject<Potion> registerPotionWithVariants(String id, MobEffect effect, ItemLike ingredient,
                                                             int duration, int longDuration, int strongDuration,
                                                             int amplifier, int strongAmplifier,
                                                             ItemLike longIngredient, ItemLike strongIngredient) {
        RegistryObject<Potion> potion = registerPotionWithRecipe(id, effect, Potions.AWKWARD, ingredient, duration, amplifier);
        registerPotionWithRecipe("long_" + id, effect, potion.get(), longIngredient, longDuration, amplifier);
        registerPotionWithRecipe("strong_" + id, effect, potion.get(), strongIngredient, strongDuration, strongAmplifier);
        return potion;
    }
}