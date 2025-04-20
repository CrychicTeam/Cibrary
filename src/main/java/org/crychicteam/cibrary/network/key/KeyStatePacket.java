package org.crychicteam.cibrary.network.key;

import dev.xkmc.l2serial.network.SerialPacketBase;
import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import org.crychicteam.cibrary.Cibrary;
import org.crychicteam.cibrary.content.event.ConfiguredKeyEvent;
import org.crychicteam.cibrary.content.key.KeyData;
import org.crychicteam.cibrary.kubejs.ConfiguredKeyEventHelper;

@SerialClass
public class KeyStatePacket extends SerialPacketBase {
    @SerialClass.SerialField
    public KeyData keyData;

    public KeyStatePacket() {}

    public KeyStatePacket(KeyData keyData) {
        this.keyData = keyData;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    Cibrary.KEY_HANDLER.updateKeyState(player, keyData);
                    if (keyData.state.equals(KeyData.KeyState.PRESSED)) {
                        var pressed_event = new ConfiguredKeyEvent.Pressed(player, keyData);
                        MinecraftForge.EVENT_BUS.post(pressed_event);
                        if (Cibrary.isLoaded("kubejs")) {
                            ConfiguredKeyEventHelper.pressed(player, keyData);
                        }
                    } else if (keyData.state.equals(KeyData.KeyState.CHARGING)) {
                        var charging_event = new ConfiguredKeyEvent.Charging(player, keyData);
                        MinecraftForge.EVENT_BUS.post(charging_event);
                        if (Cibrary.isLoaded("kubejs")) {
                            ConfiguredKeyEventHelper.charging(player, keyData);
                        }
                    } else if (keyData.state.equals(KeyData.KeyState.RELEASED)) {
                        var released_event = new ConfiguredKeyEvent.Released(player, keyData);
                        MinecraftForge.EVENT_BUS.post(released_event);
                        if (Cibrary.isLoaded("kubejs")) {
                            ConfiguredKeyEventHelper.released(player, keyData);
                        }
                    }
                }
            }
        });
    }
}