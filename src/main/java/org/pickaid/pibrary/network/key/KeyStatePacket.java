package org.pickaid.pibrary.network.key;

import dev.xkmc.l2serial.network.SerialPacketBase;
import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.event.ConfiguredKeyEvent;
import org.pickaid.pibrary.content.armorset.ArmorSet;
import org.pickaid.pibrary.content.armorset.common.ArmorSetManager;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.kubejs.ConfiguredKeyEventHelper;

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
                    ArmorSet armorSet = ArmorSetManager.getActiveArmorSet(player);
                    Pibrary.KEY_HANDLER.updateKeyState(player, keyData);
                    if (keyData.state.equals(KeyData.KeyState.PRESSED)) {
                        var pressed_event = new ConfiguredKeyEvent.Pressed(player, keyData);
                        MinecraftForge.EVENT_BUS.post(pressed_event);
                        if (Pibrary.isLoaded("kubejs")) {
                            ConfiguredKeyEventHelper.pressed(player, keyData);
                        }
                        armorSet.getEffect().onSkillPress(player, keyData);
                    } else if (keyData.state.equals(KeyData.KeyState.RELEASED)) {
                        var released_event = new ConfiguredKeyEvent.Released(player, keyData);
                        MinecraftForge.EVENT_BUS.post(released_event);
                        if (Pibrary.isLoaded("kubejs")) {
                            ConfiguredKeyEventHelper.released(player, keyData);
                        }
                        armorSet.getEffect().onSkillRelease(player, keyData);
                    }
                }
            }
        });
    }
}