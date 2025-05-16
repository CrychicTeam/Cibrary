package org.pickaid.pibrary.network.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.pickaid.pibrary.content.sound.SoundData;
import org.pickaid.pibrary.content.sound.SoundManagerHandler;

import java.util.function.Supplier;

public class SoundPacket {
    public SoundData soundData;
    public SoundData newSoundData;
    public PacketType packetType;

    public enum PacketType {
        PLAY, STOP, STOP_ALL, CROSS_FADE
    }

    public SoundPacket(SoundData soundData, PacketType packetType) {
        this.soundData = soundData;
        this.packetType = packetType;
        this.newSoundData = null;
    }

    public SoundPacket(SoundData oldSoundData, SoundData newSoundData, PacketType packetType) {
        this.soundData = oldSoundData;
        this.newSoundData = newSoundData;
        this.packetType = packetType;
    }

    public static void encode(SoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.packetType);
        packet.soundData.encode(buffer);
        if (packet.packetType == PacketType.CROSS_FADE) {
            buffer.writeBoolean(packet.newSoundData != null);
            if (packet.newSoundData != null) {
                packet.newSoundData.encode(buffer);
            }
        }
    }

    public static SoundPacket decode(FriendlyByteBuf buffer) {
        PacketType packetType = buffer.readEnum(PacketType.class);
        SoundData soundData = SoundData.decode(buffer);
        if (packetType == PacketType.CROSS_FADE && buffer.readBoolean()) {
            SoundData newSoundData = SoundData.decode(buffer);
            return new SoundPacket(soundData, newSoundData, packetType);
        } else {
            return new SoundPacket(soundData, packetType);
        }
    }

    public static void handle(SoundPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                handleClientSide(packet);
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleClientSide(SoundPacket packet) {
        SoundManagerHandler manager = SoundManagerHandler.getInstance();
        Player clientPlayer = Minecraft.getInstance().player;

        switch (packet.packetType) {
            case PLAY:
                SoundEvent sound = SoundEvent.createVariableRangeEvent(packet.soundData.sound);
                if (packet.soundData.loopCount > 0) {
                    manager.playLoopingSound(clientPlayer, sound, packet.soundData.soundType, packet.soundData.volume, packet.soundData.pitch, packet.soundData.loopCount, packet.soundData.fadeTime);
                } else {
                    manager.playSound(clientPlayer, sound, packet.soundData.soundType, packet.soundData.volume, packet.soundData.pitch, packet.soundData.fadeTime);
                }
                break;
            case STOP:
                manager.stopSound(packet.soundData.sound, packet.soundData.fadeTime);
                break;
            case STOP_ALL:
                manager.stopAllSounds(packet.soundData.fadeTime);
                break;
            case CROSS_FADE:
                if (packet.newSoundData != null) {
                    SoundEvent oldSound = SoundEvent.createVariableRangeEvent(packet.soundData.sound);
                    SoundEvent newSound = SoundEvent.createVariableRangeEvent(packet.newSoundData.sound);
                    manager.crossFade(oldSound.getLocation(), newSound, clientPlayer, packet.newSoundData.soundType, packet.newSoundData.volume, packet.newSoundData.pitch, packet.newSoundData.fadeTime);
                }
                break;
        }
    }
}