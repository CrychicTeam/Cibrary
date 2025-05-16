package org.pickaid.pibrary.content.sound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public class SoundData {
    public ResourceLocation sound;
    public SoundSource soundType;
    public float volume;
    public float pitch;
    public float fadeTime;
    public int loopCount;

    public SoundData() {}

    public SoundData(ResourceLocation sound, SoundSource soundType, float volume, float pitch, float fadeTime, int loopCount) {
        this.sound = sound;
        this.soundType = soundType;
        this.volume = volume;
        this.pitch = pitch;
        this.fadeTime = fadeTime;
        this.loopCount = loopCount;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(sound);
        buffer.writeEnum(soundType);
        buffer.writeFloat(volume);
        buffer.writeFloat(pitch);
        buffer.writeFloat(fadeTime);
        buffer.writeInt(loopCount);
    }

    public static SoundData decode(FriendlyByteBuf buffer) {
        ResourceLocation sound = buffer.readResourceLocation();
        SoundSource soundType = buffer.readEnum(SoundSource.class);
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        float fadeTime = buffer.readFloat();
        int loopCount = buffer.readInt();

        return new SoundData(sound, soundType, volume, pitch, fadeTime, loopCount);
    }
}