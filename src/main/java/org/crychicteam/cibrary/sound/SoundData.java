package org.crychicteam.cibrary.sound;

import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.resources.ResourceLocation;

@SerialClass
public class SoundData {
    @SerialClass.SerialField
    public ResourceLocation sound;

    @SerialClass.SerialField
    public float volume;

    @SerialClass.SerialField
    public float pitch;

    @SerialClass.SerialField
    public float fadeTime;

    @SerialClass.SerialField
    public int loopCount;

    public SoundData() {}

    public SoundData(ResourceLocation sound, float volume, float pitch, float fadeTime, int loopCount) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.fadeTime = fadeTime;
        this.loopCount = loopCount;
    }
}