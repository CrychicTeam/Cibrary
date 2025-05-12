package org.pickaid.pibrary.content.key.registry;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.key.combo.ComboSystem;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class KeyRegistry {
    private static final Map<ResourceLocation, KeyConfig> KEYS = new HashMap<>();

    public static KeyConfig register(KeyConfig config) {
        KEYS.put(config.id, config);
        Pibrary.LOGGER.info("Registered key: " + config.id);
        return config;
    }

    @SubscribeEvent
    public static void onKeyMappingRegister(RegisterKeyMappingsEvent event) {
        for (KeyConfig config : getAllConfigs()) {
            for (KeyMapping keyMapping : Minecraft.getInstance().options.keyMappings) {
                if (!keyMapping.equals(config.keyMapping)) {
                    event.register(config.keyMapping);
                }
            }
        }
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