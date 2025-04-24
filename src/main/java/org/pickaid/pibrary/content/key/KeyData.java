package org.pickaid.pibrary.content.key;

import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.resources.ResourceLocation;

@SerialClass
public class KeyData {
    @SerialClass.SerialField
    public ResourceLocation keyId;

    @SerialClass.SerialField
    public KeyState state;

    @SerialClass.SerialField
    public float power;

    @SerialClass.SerialField
    public float remainingTime;

    public KeyData() {
        this.state = KeyState.IDLE;
        this.power = 0;
        this.remainingTime = 0;
    }

    public KeyData(ResourceLocation keyId) {
        this();
        this.keyId = keyId;
    }

    public void reset() {
        this.state = KeyState.IDLE;
        this.power = 0;
    }

    @SerialClass
    public enum KeyState {
        IDLE,
        PRESSED,
        CHARGING,
        RELEASED
    }
}