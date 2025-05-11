package org.pickaid.pibrary.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;
import org.pickaid.pibrary.content.key.KeyData;

public class ConfiguredKeyEvent extends Event {
    private final Player player;
    private final KeyData keyData;

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

    public static class Finished extends ConfiguredKeyEvent {
        public Finished(Player player, KeyData keyData) {
            super(player, keyData);
        }
    }

    public static class RapidClick extends ConfiguredKeyEvent {
        public RapidClick(Player player, KeyData keyData) {
            super(player, keyData);
        }
    }

    public static class RapidClickFinish extends ConfiguredKeyEvent {
        public RapidClickFinish(Player player, KeyData keyData) {
            super(player, keyData);
        }
    }

    public static class TimeOut extends ConfiguredKeyEvent {
        public TimeOut(Player player, KeyData keyData) {
            super(player, keyData);
        }
    }

    public static class Held extends ConfiguredKeyEvent {
        public Held(Player player, KeyData keyData) {
            super(player, keyData);
        }
    }

    public static class HeldReleased extends ConfiguredKeyEvent {
        public HeldReleased(Player player, KeyData keyData) {
            super(player, keyData);
        }
    }
}