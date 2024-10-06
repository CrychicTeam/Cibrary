package org.crychicteam.cibrary.sound;

import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

@SerialClass
public class SoundData {
    @SerialClass.SerialField
    public ResourceLocation sound;

    @SerialClass.SerialField
    public SoundSource soundType;

    @SerialClass.SerialField
    public float volume;

    @SerialClass.SerialField
    public float pitch;

    @SerialClass.SerialField
    public float fadeTime;

    @SerialClass.SerialField
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
}