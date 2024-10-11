package org.crychicteam.cibrary.content.armorset.common;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import org.crychicteam.cibrary.Cibrary;
import org.crychicteam.cibrary.content.armorset.ArmorSet;
import org.crychicteam.cibrary.content.armorset.defaults.DefaultArmorSet;

import java.util.*;

public class ArmorSetRegistry {
    protected final List<ArmorSet> armorSets = new ArrayList<>();
    protected final Map<String, ArmorSet> armorSetMap = new HashMap<>();
    protected final Map<Item, Set<ArmorSet>> itemToSetIndex = new Object2ObjectOpenHashMap<>();
    protected static ArmorSet defaultArmorSet;

    protected ArmorSetRegistry() {
        defaultArmorSet = new DefaultArmorSet();
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

    public Map<Item, Set<ArmorSet>> getItemToSetIndex() {
        return itemToSetIndex;
    }

    public ArmorSet getDefaultArmorSet() {
        return defaultArmorSet;
    }
}