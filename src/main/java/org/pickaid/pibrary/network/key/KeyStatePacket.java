package org.pickaid.pibrary.network.key;

import dev.xkmc.l2serial.network.SerialPacketBase;
import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.common.ServerKeyManager;
import org.pickaid.pibrary.api.event.ConfiguredKeyEvent;
import org.pickaid.pibrary.content.key.KeyData;

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
                    Pibrary.KEY_HANDLER.updateKeyState(player, keyData);

                    switch (keyData.state) {
                        case PRESSED:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.Pressed(player, keyData));
                            ServerKeyManager.chargingKeys.remove(keyData.keyId);
                            ServerKeyManager.heldClickKeys.remove(keyData.keyId);
                            break;
                        case RELEASED:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.Released(player, keyData));
                            ServerKeyManager.chargingKeys.remove(keyData.keyId);
                            ServerKeyManager.heldClickKeys.remove(keyData.keyId);
                            break;
                        case FINISHED:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.Finished(player, keyData));
                            ServerKeyManager.chargingKeys.remove(keyData.keyId);
                            ServerKeyManager.heldClickKeys.remove(keyData.keyId);
                            break;
                        case RAPID_CLICK:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.RapidClick(player, keyData));
                            break;
                        case RAPID_FINISH:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.RapidClickFinish(player, keyData));
                            break;
                        case TIMEOUT:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.TimeOut(player, keyData));
                            ServerKeyManager.chargingKeys.remove(keyData.keyId);
                            ServerKeyManager.heldClickKeys.remove(keyData.keyId);
                            break;
                        case HELD_RELEASED:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.HeldReleased(player, keyData));
                            ServerKeyManager.chargingKeys.remove(keyData.keyId);
                            ServerKeyManager.heldClickKeys.remove(keyData.keyId);
                            break;
                        case CHARGING:
                            ServerKeyManager.chargingKeys.add(keyData.keyId);
                            break;
                        case HELD:
                            ServerKeyManager.heldClickKeys.add(keyData.keyId);
                            break;
                        case IDLE:
                        case COOLDOWN:
                        case AWAITING_RELEASE:
                            ServerKeyManager.chargingKeys.remove(keyData.keyId);
                            ServerKeyManager.heldClickKeys.remove(keyData.keyId);
                            break;
                        default:
                            Pibrary.LOGGER.warn("Received unknown state: {}", keyData.state);
                            break;
                    }
                }
            }
        });
    }
}