package org.crychicteam.cibrary.api.registry.armorset;

import org.crychicteam.cibrary.Cibrary;
import org.crychicteam.cibrary.content.armorset.ArmorSet;

import java.util.LinkedHashSet;
import java.util.Set;

public class ArmorSetRegistry {
    private static final Set<ArmorSet> armorSets = new LinkedHashSet<>();

    private ArmorSetRegistry() {}

    public static ArmorSet register(ArmorSet armorSet) {
        if (armorSets.add(armorSet)) {
            return armorSet;
        } else {
            throw new IllegalArgumentException("ArmorSet with identifier " + armorSet.getIdentifier() + " already registered");
        }
    }

    public static void registerAll() {
        for (ArmorSet armorSet : armorSets) {
            Cibrary.ARMOR_SET_MANAGER.registerArmorSet(armorSet);
        }
        armorSets.clear();
    }

    public static void initializeAll() {
        registerAll();
    }
}