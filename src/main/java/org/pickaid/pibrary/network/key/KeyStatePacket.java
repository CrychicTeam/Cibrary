package org.pickaid.pibrary.network.key;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import org.pickaid.pibrary.Pibrary;;
import org.pickaid.pibrary.api.common.ServerKeyManager;
import org.pickaid.pibrary.api.event.ConfiguredKeyEvent;
import org.pickaid.pibrary.content.key.KeyData;

import java.util.function.Supplier;

public record KeyStatePacket(KeyData keyData) {

    public KeyStatePacket {}

    public static void encode(KeyStatePacket packet, FriendlyByteBuf buffer) {
        packet.keyData.encode(buffer);
    }

    public static KeyStatePacket decode(FriendlyByteBuf buffer) {
        return new KeyStatePacket(KeyData.decode(buffer));
    }

    public static void handle(KeyStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    Pibrary.KEY_HANDLER.updateKeyState(player, packet.keyData);

                    switch (packet.keyData.state) {
                        case PRESSED:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.Pressed(player, packet.keyData));
                            ServerKeyManager.chargingKeys.remove(packet.keyData.keyId);
                            ServerKeyManager.heldClickKeys.remove(packet.keyData.keyId);
                            break;
                        case RELEASED:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.Released(player, packet.keyData));
                            ServerKeyManager.chargingKeys.remove(packet.keyData.keyId);
                            ServerKeyManager.heldClickKeys.remove(packet.keyData.keyId);
                            break;
                        case FINISHED:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.Finished(player, packet.keyData));
                            ServerKeyManager.chargingKeys.remove(packet.keyData.keyId);
                            ServerKeyManager.heldClickKeys.remove(packet.keyData.keyId);
                            break;
                        case RAPID_CLICK:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.RapidClick(player, packet.keyData));
                            break;
                        case RAPID_FINISH:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.RapidClickFinish(player, packet.keyData));
                            break;
                        case TIMEOUT:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.TimeOut(player, packet.keyData));
                            ServerKeyManager.chargingKeys.remove(packet.keyData.keyId);
                            ServerKeyManager.heldClickKeys.remove(packet.keyData.keyId);
                            break;
                        case HELD_RELEASED:
                            MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.HeldReleased(player, packet.keyData));
                            ServerKeyManager.chargingKeys.remove(packet.keyData.keyId);
                            ServerKeyManager.heldClickKeys.remove(packet.keyData.keyId);
                            break;
                        case CHARGING:
                            ServerKeyManager.chargingKeys.add(packet.keyData.keyId);
                            break;
                        case HELD:
                            ServerKeyManager.heldClickKeys.add(packet.keyData.keyId);
                            break;
                        case IDLE:
                        case COOLDOWN:
                        case AWAITING_RELEASE:
                            ServerKeyManager.chargingKeys.remove(packet.keyData.keyId);
                            ServerKeyManager.heldClickKeys.remove(packet.keyData.keyId);
                            break;
                        default:
                            Pibrary.LOGGER.warn("Received unknown state: {}", packet.keyData.state);
                            break;
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}