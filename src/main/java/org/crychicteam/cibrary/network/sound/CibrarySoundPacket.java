package org.crychicteam.cibrary.network.sound;

import dev.xkmc.l2serial.network.SerialPacketBase;
import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraftforge.network.NetworkEvent;
import org.crychicteam.cibrary.sound.CibrarySoundManagerHandler;
import org.crychicteam.cibrary.sound.SoundData;

import net.minecraft.sounds.SoundEvent;

@SerialClass
public class CibrarySoundPacket extends SerialPacketBase {
    @SerialClass.SerialField
    public SoundData soundData;

    @SerialClass.SerialField
    public SoundData newSoundData; // For cross-fade

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
        CibrarySoundManagerHandler manager = CibrarySoundManagerHandler.getInstance();
        SoundEvent sound = SoundEvent.createVariableRangeEvent(soundData.sound);

        switch (packetType) {
            case PLAY:
                if (soundData.loopCount > 0) {
                    manager.playLoopingSound(null, sound, soundData.volume, soundData.pitch, soundData.loopCount, soundData.fadeTime);
                } else {
                    manager.playSound(null, sound, soundData.volume, soundData.pitch, soundData.fadeTime);
                }
                break;
            case STOP:
                manager.stopSound(sound, soundData.fadeTime);
                break;
            case STOP_ALL:
                manager.stopAllSounds(soundData.fadeTime);
                break;
            case CROSS_FADE:
                if (newSoundData != null) {
                    SoundEvent newSound = SoundEvent.createVariableRangeEvent(newSoundData.sound);
                    manager.crossFade(sound, newSound, null, newSoundData.volume, newSoundData.pitch, newSoundData.fadeTime);
                }
                break;
        }
    }
}