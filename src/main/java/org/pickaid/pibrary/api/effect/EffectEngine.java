package org.pickaid.pibrary.api.effect;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class EffectEngine {
    private static final Map<Player, IPlayerEffect> activePlayerEffects = new HashMap<>();

    public static Map<Player, IPlayerEffect> getActivePlayerEffects() {
        return activePlayerEffects;
    }

    /**
     * Push a player effect to the engine to automatically handle it.
     * @param player the player entity
     * @param effect the player effect to apply
     */
    public static void activePlayerEffect(Player player, IPlayerEffect effect) {
        activePlayerEffects.put(player, effect);
    }
}
