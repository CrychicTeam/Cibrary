package org.crychicteam.cibrary.api.common;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class CibraryEnchantHelper {

    /**
     * Randomly enchants all of a living entity's equipment.
     */
    public static void randomEnchantLivingEntityEquipment(LivingEntity entity,
                                                          Predicate<Enchantment> enchantmentPredicate,
                                                          EnchantmentLevelSelector levelSelector,
                                                          int maxEnchantsPerSession,
                                                          RandomSource random) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = entity.getItemBySlot(slot);
            if (!item.isEmpty()) {
                addRandomEnchantments(item, enchantmentPredicate, levelSelector, maxEnchantsPerSession, random);
            }
        }
    }

    /**
     * Adds random enchantments to an ItemStack, considering existing enchantments for compatibility
     */
    public static ItemStack addRandomEnchantments(ItemStack stack,
                                                  Predicate<Enchantment> enchantmentPredicate,
                                                  EnchantmentLevelSelector levelSelector,
                                                  int maxEnchantsPerSession,
                                                  RandomSource random) {
        Map<Enchantment, Integer> existingEnchantments = EnchantmentHelper.getEnchantments(stack);
        List<EnchantmentInstance> availableEnchantments = getAvailableEnchantmentResults(30, stack, true);
        availableEnchantments.removeIf(instance -> !enchantmentPredicate.test(instance.enchantment) ||
                !isCompatibleWithExisting(instance.enchantment, existingEnchantments));

        int enchantmentsAdded = 0;
        while (enchantmentsAdded < maxEnchantsPerSession && !availableEnchantments.isEmpty()) {
            Optional<EnchantmentInstance> selectedEnchantment = WeightedRandom.getRandomItem(random, availableEnchantments);
            if (selectedEnchantment.isPresent()) {
                EnchantmentInstance instance = selectedEnchantment.get();
                int level = levelSelector.selectLevel(instance.enchantment, random);
                addOrUpdateEnchantment(stack, instance.enchantment, level);
                enchantmentsAdded++;
                EnchantmentHelper.filterCompatibleEnchantments(availableEnchantments, instance);
            } else {
                break;
            }
        }

        return stack;
    }

    /**
     * Adds a new enchantment or updates an existing one, respecting max levels
     */
    private static void addOrUpdateEnchantment(ItemStack stack, Enchantment enchantment, int newLevel) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        int existingLevel = enchantments.getOrDefault(enchantment, 0);
        int finalLevel = Math.min(enchantment.getMaxLevel(), existingLevel + newLevel);
        enchantments.put(enchantment, finalLevel);
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    private static boolean isCompatibleWithExisting(Enchantment enchantment, Map<Enchantment, Integer> existingEnchantments) {
        return EnchantmentHelper.isEnchantmentCompatible(existingEnchantments.keySet(), enchantment);
    }

    public static List<Enchantment> getApplicableEnchantments(ItemStack stack, Predicate<Enchantment> additionalPredicate) {
        List<Enchantment> applicableEnchantments = new ArrayList<>();
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            if (enchantment.canEnchant(stack) && additionalPredicate.test(enchantment)) {
                applicableEnchantments.add(enchantment);
            }
        }
        return applicableEnchantments;
    }

    public static int getTotalEnchantmentLevelForEntity(LivingEntity entity) {
        int totalLevel = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = entity.getItemBySlot(slot);
            if (!item.isEmpty()) {
                totalLevel += getTotalEnchantmentLevel(item);
            }
        }
        return totalLevel;
    }

    public static int getTotalEnchantmentLevel(ItemStack stack) {
        return EnchantmentHelper.getEnchantments(stack).values().stream().mapToInt(Integer::intValue).sum();
    }

    @FunctionalInterface
    public interface EnchantmentLevelSelector {
        int selectLevel(Enchantment enchantment, RandomSource random);
    }

    private static List<EnchantmentInstance> getAvailableEnchantmentResults(int level, ItemStack stack, boolean allowTreasure) {
        return EnchantmentHelper.getAvailableEnchantmentResults(level, stack, allowTreasure);
    }
}