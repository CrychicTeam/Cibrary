package org.pickaid.pibrary.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.combo.ComboDefinition;
import org.pickaid.pibrary.content.sound.SoundData;
import org.pickaid.pibrary.network.key.ComboPacket;
import org.pickaid.pibrary.network.key.KeyStatePacket;
import org.pickaid.pibrary.network.key.SetKeyStatePacket;
import org.pickaid.pibrary.network.sound.SoundPacket;

public class PibraryNetworkHandler {
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Pibrary.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(packetId++, KeyStatePacket.class, KeyStatePacket::encode, KeyStatePacket::decode, KeyStatePacket::handle);
        CHANNEL.registerMessage(packetId++, SetKeyStatePacket.class, SetKeyStatePacket::encode, SetKeyStatePacket::decode, SetKeyStatePacket::handle);
        CHANNEL.registerMessage(packetId++, ComboPacket.class, ComboPacket::encode, ComboPacket::decode, ComboPacket::handle);
        CHANNEL.registerMessage(packetId++, SoundPacket.class, SoundPacket::encode, SoundPacket::decode, SoundPacket::handle);
    }

    public static void setSound(ServerPlayer player, SoundData soundData, SoundPacket.PacketType type) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SoundPacket(soundData, type));
    }

    public static void fadeSound(ServerPlayer player, SoundData oldSoundData, SoundData newSoundData, SoundPacket.PacketType type) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SoundPacket(oldSoundData, newSoundData, type));
    }

    public static void setKeyState(ServerPlayer player, KeyData keyData) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SetKeyStatePacket(keyData));
    }

    public static void combo(ComboDefinition definition) {
        CHANNEL.sendToServer(new ComboPacket(definition.getId()));
    }
}
