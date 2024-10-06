package org.crychicteam.cibrary.network.sound;

import dev.xkmc.l2serial.network.SerialPacketBase;
import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.crychicteam.cibrary.CibraryClient;
import org.crychicteam.cibrary.content.sound.CibrarySoundManagerHandler;
import org.crychicteam.cibrary.content.sound.SoundData;

@SerialClass
public class CibrarySoundPacket extends SerialPacketBase {
    @SerialClass.SerialField
    public SoundData soundData;

    @SerialClass.SerialField
    public SoundData newSoundData;

    @SerialClass.SerialField
    public PacketType packetType;

    public enum PacketType {
        PLAY, STOP, STOP_ALL, CROSS_FADE
    }

    public CibrarySoundPacket() {}

    public CibrarySoundPacket(SoundData soundData, PacketType packetType) {
        this.soundData = soundData;
        this.packetType = packetType;
    }

    public CibrarySoundPacket(SoundData oldSoundData, SoundData newSoundData, PacketType packetType) {
        this.soundData = oldSoundData;
        this.newSoundData = newSoundData;
        this.packetType = packetType;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                handleClientSide();
            }
        });
    }

    private void handleClientSide() {
        CibrarySoundManagerHandler manager = CibraryClient.CLIENT_SOUND_MANAGER;
        Player clientPlayer = Minecraft.getInstance().player;

        switch (packetType) {
            case PLAY:
                SoundEvent sound = SoundEvent.createVariableRangeEvent(soundData.sound);
                if (soundData.loopCount > 0) {
                    manager.playLoopingSound(clientPlayer, sound, soundData.soundType, soundData.volume, soundData.pitch, soundData.loopCount, soundData.fadeTime);
                } else {
                    manager.playSound(clientPlayer, sound, soundData.soundType, soundData.volume, soundData.pitch, soundData.fadeTime);
                }
                break;
            case STOP:
                manager.stopSound(soundData.sound, soundData.fadeTime);
                break;
            case STOP_ALL:
                manager.stopAllSounds(soundData.fadeTime);
                break;
            case CROSS_FADE:
                if (newSoundData != null) {
                    SoundEvent oldSound = SoundEvent.createVariableRangeEvent(soundData.sound);
                    SoundEvent newSound = SoundEvent.createVariableRangeEvent(newSoundData.sound);
                    manager.crossFade(oldSound.getLocation(), newSound, clientPlayer, newSoundData.soundType, newSoundData.volume, newSoundData.pitch, newSoundData.fadeTime);
                }
                break;
        }
    }
}