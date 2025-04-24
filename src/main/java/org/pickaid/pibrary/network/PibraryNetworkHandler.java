package org.pickaid.pibrary.network;

import dev.xkmc.l2serial.network.BasePacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.network.key.KeyStatePacket;
import org.pickaid.pibrary.network.sound.SoundPacket;
import org.pickaid.pibrary.network.armorset.ArmorSetSyncPacket;
import org.pickaid.pibrary.content.armorset.capability.IArmorSetCapability;

public class PibraryNetworkHandler {
    public static final BasePacketHandler HANDLER = new BasePacketHandler(
            new ResourceLocation(Pibrary.MOD_ID, "main"),
            1,
            handler -> handler.create(SoundPacket.class, NetworkDirection.PLAY_TO_CLIENT),
            handler -> handler.create(ArmorSetSyncPacket.class, NetworkDirection.PLAY_TO_CLIENT),
            handler -> handler.create(KeyStatePacket.class, NetworkDirection.PLAY_TO_SERVER)
    );

    public static void init() {
        HANDLER.registerPackets();
    }

    public static void sendArmorSetSync(ServerPlayer player, IArmorSetCapability cap) {
        HANDLER.toClientPlayer(new ArmorSetSyncPacket(cap), player);
    }
}