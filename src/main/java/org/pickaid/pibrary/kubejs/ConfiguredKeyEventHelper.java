package org.pickaid.pibrary.kubejs;

import net.minecraft.world.entity.player.Player;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.kubejs.event.ConfiguredKeyEventJS;

public class ConfiguredKeyEventHelper {

    public static void pressed(Player player, KeyData keyData) {
        var kjs_pressed_event = new ConfiguredKeyEventJS.Pressed(player, keyData);
        PiEventS.PRESSED.post(kjs_pressed_event);
    }

    public static void charging(Player player, KeyData keyData) {
        var kjs_charging_event = new ConfiguredKeyEventJS.Charginng(player, keyData);
        PiEventS.CHARGING.post(kjs_charging_event);
    }

    public static void released(Player player, KeyData keyData) {
        var kjs_released_event = new ConfiguredKeyEventJS.Released(player, keyData);
        PiEventS.RELEASED.post(kjs_released_event);
    }
}
