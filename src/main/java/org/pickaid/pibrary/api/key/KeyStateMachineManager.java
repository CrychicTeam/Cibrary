package org.pickaid.pibrary.api.key;

import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.content.key.KeyStateMachine;
import org.pickaid.pibrary.content.key.registry.KeyConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KeyStateMachineManager {
    private static final Map<ResourceLocation, KeyStateMachine> STATE_MACHINES = new HashMap<>();

    public static KeyStateMachine getOrCreate(KeyConfig config) {
        return STATE_MACHINES.computeIfAbsent(config.id, id -> new KeyStateMachine(config));
    }

    public static Optional<KeyStateMachine> getStateMachine(ResourceLocation keyId) {
        return Optional.ofNullable(STATE_MACHINES.get(keyId));
    }

    public static void remove(ResourceLocation keyId) {
        STATE_MACHINES.remove(keyId);
    }
}