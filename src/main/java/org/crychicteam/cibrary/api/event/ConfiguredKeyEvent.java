package org.crychicteam.cibrary.api.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;
import org.crychicteam.cibrary.content.key.KeyData;

public class ConfiguredKeyEvent extends Event {
    private Player player;
    private KeyData keyData;

    public ConfiguredKeyEvent(Player player, KeyData keyData) {
        this.player = player;
        this.keyData = keyData;
    }

    public Player getPlayer() {
        return player;
    }

    public KeyData getKeyData() {
        return keyData;
    }

    public static class Pressed extends ConfiguredKeyEvent {

        public Pressed(Player player, KeyData keyData) {
            super(player, keyData);
        }
    }

    public static class Charging extends ConfiguredKeyEvent {

        public Charging(Player player, KeyData keyData) {
            super(player, keyData);
        }
    }

    public static class Released extends ConfiguredKeyEvent {

        public Released(Player player, KeyData keyData) {
            super(player, keyData);
        }
    }
}
