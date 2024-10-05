package org.crychicteam.cibrary.network;

import dev.xkmc.l2serial.network.BasePacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import org.crychicteam.cibrary.Cibrary;
import org.crychicteam.cibrary.network.sound.CibrarySoundPacket;

public class CibraryNetworkHandler {
    public static final BasePacketHandler HANDLER = new BasePacketHandler(
            new ResourceLocation(Cibrary.MOD_ID, "main"),
            1,
            handler -> handler.create(CibrarySoundPacket.class, NetworkDirection.PLAY_TO_CLIENT)
    );

    public static void init() {
        HANDLER.registerPackets();
    }
}