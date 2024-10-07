package org.crychicteam.cibrary.content.armorset;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.crychicteam.cibrary.Cibrary;
import org.crychicteam.cibrary.content.armorset.capability.ArmorSetCapability;
import org.crychicteam.cibrary.network.CibraryNetworkHandler;

import java.util.*;

public class ArmorSetManager {
    private final List<ArmorSet> armorSets = new ArrayList<>();
    private final Map<String, ArmorSet> armorSetMap = new HashMap<>();
    private final Map<Item, Set<ArmorSet>> itemToSetIndex = new Object2ObjectOpenHashMap<>();
    private static ArmorSetManager instance;
    private static ArmorSet defaultArmorSet;

    private ArmorSetManager() {
        defaultArmorSet = new DefaultArmorSet();
    }

    public static ArmorSetManager getInstance() {
        if (instance == null) {
            instance = new ArmorSetManager();
        }
        return instance;
    }

    public void registerArmorSet(ArmorSet armorSet) {
        if (!isExactSetExists(armorSet)) {
            armorSets.add(armorSet);
            armorSetMap.put(armorSet.getIdentifier(), armorSet);
            indexArmorSet(armorSet);
        }
        Cibrary.LOGGER.info("Loaded armor set: " + armorSet.getIdentifier());
    }

    private void indexArmorSet(ArmorSet armorSet) {
        for (Map.Entry<EquipmentSlot, Set<Item>> entry : armorSet.getEquipmentItems().entrySet()) {
            for (Item item : entry.getValue()) {
                itemToSetIndex.computeIfAbsent(item, k -> new HashSet<>()).add(armorSet);
            }
        }
    }

    private boolean isExactSetExists(ArmorSet newSet) {
        return armorSets.stream().anyMatch(set -> areSetItemsIdentical(set, newSet));
    }

    private boolean areSetItemsIdentical(ArmorSet set1, ArmorSet set2) {
        return Objects.equals(set1.getEquipmentItems(), set2.getEquipmentItems());
    }

    public ArmorSet getArmorSetByIdentifier(String identifier) {
        return armorSetMap.getOrDefault(identifier, defaultArmorSet);
    }

    public void syncCapability(Player player) {
        player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(cap -> {
            updateEntitySetEffect(player);
        });
    }
    public ArmorSet getActiveArmorSet(Player player) {
        if (player == null) {
            return null;
        }
        return player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY)
                .map(ArmorSetCapability::getActiveSet)
                .orElse(null);
    }

    public void updateEntitySetEffect(LivingEntity entity) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(armorSetCap -> {
                ArmorSet currentSet = armorSetCap.getActiveSet();
                ArmorSet newMatchedSet = findMatchingArmorSet(player);

                if (currentSet != newMatchedSet) {
                    if (currentSet != null) {
                        removeArmorSetEffect(player, currentSet);
                    }
                    if (newMatchedSet != null) {
                        applyArmorSetEffect(player, newMatchedSet);
                    }
                    armorSetCap.setActiveSet(newMatchedSet);
                    CibraryNetworkHandler.sendArmorSetSync((ServerPlayer) player, armorSetCap);
                }
            });
        }
    }

    private ArmorSet findMatchingArmorSet(LivingEntity entity) {
        Set<ArmorSet> potentialSets = new HashSet<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Item item = entity.getItemBySlot(slot).getItem();
            Set<ArmorSet> setsForItem = itemToSetIndex.get(item);
            if (setsForItem != null) {
                if (potentialSets.isEmpty()) {
                    potentialSets.addAll(setsForItem);
                } else {
                    potentialSets.retainAll(setsForItem);
                }
            }
        }

        return potentialSets.stream()
                .filter(set -> set.matches(entity))
                .findFirst()
                .orElse(defaultArmorSet);
    }

    private void applyArmorSetEffect(LivingEntity entity, ArmorSet armorSet) {
        armorSet.applyEffectsAndAttributes(entity);
        SetEffect effect = armorSet.getEffect();
        if (effect != null) {
            effect.applyEffect(entity);
        }
    }

    private void removeArmorSetEffect(LivingEntity entity, ArmorSet armorSet) {
        armorSet.removeEffectsAndAttributes(entity);
        SetEffect effect = armorSet.getEffect();
        if (effect != null) {
            effect.removeEffect(entity);
        }
    }

    public ArmorSet getDefaultArmorSet () { return defaultArmorSet; }
}