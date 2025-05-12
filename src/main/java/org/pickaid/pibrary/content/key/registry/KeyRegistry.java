package org.pickaid.pibrary.content.key.registry;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.key.combo.ComboRegistrationSample;
import org.pickaid.pibrary.content.key.combo.ComboSystem;

import java.util.HashMap;
import java.util.Map;

public class KeyRegistry {
    private static final Map<ResourceLocation, KeyConfig> KEYS = new HashMap<>();
    private static boolean combosInitialized = false;

    public static KeyConfig register(KeyConfig config) {
        KEYS.put(config.id, config);
        Pibrary.LOGGER.info("Registered key: " + config.id);

        if (!combosInitialized) {
            combosInitialized = true;
            initializeCombos();
        }
        return config;
    }

    private static void initializeCombos() {
        ComboSystem.getInstance();
        ComboRegistrationSample.registerCombos();
        Pibrary.LOGGER.info("Initialized combo system");
    }

    public static ImmutableList<KeyConfig> getAllConfigs() {
        return ImmutableList.copyOf(KEYS.values());
    }

    public static KeyConfig getConfig(ResourceLocation keyId) {
        return KEYS.get(keyId);
    }

    public static void reset() {
        KEYS.clear();
        ComboSystem.getInstance().resetCombo();
    }
}