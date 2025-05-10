package org.pickaid.pibrary.content.key.registry;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.events.client.ClientKeyHandler;
import org.pickaid.pibrary.content.key.KeyData;

import java.util.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Pibrary.MOD_ID, value = Dist.CLIENT)
public class KeyRegistry {
    private static final Map<ResourceLocation, ConfiguredKey> REGISTERED_KEYS = new LinkedHashMap<>();
    private static boolean isInitialized = false;

    public static ConfiguredKey register(ConfiguredKey config) {
        if (isInitialized) {
            throw new IllegalStateException("Cannot register keys after initialization");
        }

        if (REGISTERED_KEYS.containsKey(config.id)) {
            throw new IllegalArgumentException("Duplicate key registration: " + config.id);
        }

        REGISTERED_KEYS.put(config.id, config);
        return config;
    }

    public static ConfiguredKey getConfig(ResourceLocation id) {
        return REGISTERED_KEYS.get(id);
    }

    public static Collection<ConfiguredKey> getAllConfigs() {
        return Collections.unmodifiableCollection(REGISTERED_KEYS.values());
    }

    @SubscribeEvent
    public static void onKeyMappingRegister(RegisterKeyMappingsEvent event) {
        KeyMapping[] existingMappings = Minecraft.getInstance().options.keyMappings;
        Set<String> existingMappingNames = new HashSet<>();
        for (KeyMapping existing : existingMappings) {
            existingMappingNames.add(existing.getName());
        }
        for (ConfiguredKey config : REGISTERED_KEYS.values()) {
            if (existingMappingNames.contains(config.keyMapping.getName())) {
                Pibrary.LOGGER.debug("Skipping registration for existing key mapping: {}", config.id);
            } else {
                event.register(config.keyMapping);
                Pibrary.LOGGER.debug("Registered new key mapping for: {}", config.id);
            }
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        for (ConfiguredKey config : REGISTERED_KEYS.values()) {
            registerKey(config);
        }
    }


    /**
     * Registers a new key binding in the handler's state management system.
     * <p>
     * Implementation Details:
     * <p>
     * 1. Performs duplicate registration check (O(1))
     * <p>
     * 2. Initializes all state maps with default values
     * <p>
     * Memory Impact:
     * <p>
     * - Adds entries to tracking maps
     * <p>
     *
     * @param config Configuration object containing key binding parameters
     */
    public static void registerKey(ConfiguredKey config) {
        if (ClientKeyHandler.KEY_STATES.containsKey(config.id)) {
            Pibrary.LOGGER.warn("Duplicate key registration attempt: {}", config.id);
            return;
        }
        ClientKeyHandler.KEY_STATES.put(config.id, new KeyData(config.id));
        ClientKeyHandler.PREVIOUS_STATES.put(config.id, false);
        ClientKeyHandler.PRESS_START_TIMES.put(config.id, 0L);
        ClientKeyHandler.LAST_SENT_STATE.put(config.id, KeyData.KeyState.IDLE);
        Pibrary.LOGGER.debug("Registered key: {}", config.id);
    }
}