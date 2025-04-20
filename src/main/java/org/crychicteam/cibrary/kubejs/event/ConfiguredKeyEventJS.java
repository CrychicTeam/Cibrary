package org.crychicteam.cibrary.kubejs.event;

import dev.latvian.mods.kubejs.player.PlayerEventJS;
import net.minecraft.world.entity.player.Player;
import org.crychicteam.cibrary.content.key.KeyData;

public class ConfiguredKeyEventJS extends PlayerEventJS {
    private Player player;
    private KeyData keyData;

    public ConfiguredKeyEventJS(Player player, KeyData keyData) {
        this.player = player;
        this.keyData = keyData;
    }

    @Override
    public Player getEntity() {
        return this.player;
    }

    public KeyData getKeyData() {
        return this.keyData;
    }

    public static class Pressed extends ConfiguredKeyEventJS {

        public Pressed(Player player, KeyData keyData) {
            super(player, keyData);
        }
    }

    public static class Charginng extends ConfiguredKeyEventJS {

        public Charginng(Player player, KeyData keyData) {
            super(player, keyData);
        }
    }

    public static class Released extends ConfiguredKeyEventJS {

        public Released(Player player, KeyData keyData) {
            super(player, keyData);
        }
    }
}
