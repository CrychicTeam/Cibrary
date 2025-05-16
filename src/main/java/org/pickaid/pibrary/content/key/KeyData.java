package org.pickaid.pibrary.content.key;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

public class KeyData {
    public ResourceLocation keyId;
    public KeyState state;
    public float power;
    public float remainingTime;
    public int rapidClickCount;
    public long[] pressTimestamps = new long[5];
    public long cooldownEndTime = 0L;
    public boolean inCooldown = false;
    public boolean inRapidClickCooldown = false;

    public KeyData() {
        reset();
    }

    public KeyData(ResourceLocation keyId) {
        this();
        this.keyId = keyId;
    }

    public KeyData(ResourceLocation keyId, KeyState state, float power, float remainingTime,
                   int rapidClickCount, long[] pressTimestamps, long cooldownEndTime,
                   boolean inCooldown, boolean inRapidClickCooldown) {
        this.keyId = keyId;
        this.state = state;
        this.power = power;
        this.remainingTime = remainingTime;
        this.rapidClickCount = rapidClickCount;
        this.pressTimestamps = pressTimestamps;
        this.cooldownEndTime = cooldownEndTime;
        this.inCooldown = inCooldown;
        this.inRapidClickCooldown = inRapidClickCooldown;
    }

    public void reset() {
        state = KeyState.IDLE;
        power = 0.0f;
        remainingTime = 0.0f;
        rapidClickCount = 0;
        inRapidClickCooldown = false;
        inCooldown = false;
        pressTimestamps = new long[5];
    }

    public enum KeyState {
        IDLE,
        CHARGING,
        PRESSED,
        RELEASED,
        FINISHED,
        HELD,
        HELD_RELEASED,
        RAPID_CLICK,
        RAPID_FINISH,
        TIMEOUT,
        COOLDOWN,
        AWAITING_RELEASE
    }

    public KeyData copy() {
        KeyData copy = new KeyData(this.keyId);
        copy.state = this.state;
        copy.power = this.power;
        copy.remainingTime = this.remainingTime;
        copy.rapidClickCount = this.rapidClickCount;
        copy.inCooldown = this.inCooldown;
        copy.inRapidClickCooldown = this.inRapidClickCooldown;
        copy.cooldownEndTime = this.cooldownEndTime;

        if (this.pressTimestamps != null) {
            System.arraycopy(this.pressTimestamps, 0, copy.pressTimestamps, 0,
                    Math.min(this.pressTimestamps.length, copy.pressTimestamps.length));
        }

        return copy;
    }

    public void startCooldown(long duration) {
        if (duration <= 0) return;

        inCooldown = true;
        cooldownEndTime = System.currentTimeMillis() + duration;
        state = KeyState.COOLDOWN;
    }

    public boolean checkCooldown(long currentTime) {
        if (!inCooldown) return false;

        if (currentTime >= cooldownEndTime) {
            inCooldown = false;
            cooldownEndTime = 0;
            return false;
        }

        return true;
    }

    public long getRemainingCooldown(long currentTime) {
        if (!inCooldown || cooldownEndTime <= currentTime) return 0;
        return cooldownEndTime - currentTime;
    }

    public void updatePower(float newPower, float maxPower) {
        power = Math.min(newPower, maxPower);
    }

    public void recordPressTimestamp(long timestamp) {
        if (pressTimestamps.length > 1) {
            System.arraycopy(pressTimestamps, 0, pressTimestamps, 1, pressTimestamps.length - 1);
        }
        pressTimestamps[0] = timestamp;
    }

    public void clearTimestamps() {
        Arrays.fill(pressTimestamps, 0);
    }

    @Override
    public String toString() {
        return String.format("KeyData[%s, state=%s, power=%.2f, clicks=%d]",
                keyId, state, power, rapidClickCount);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(keyId);
        buffer.writeEnum(state);
        buffer.writeFloat(power);
        buffer.writeFloat(remainingTime);
        buffer.writeInt(rapidClickCount);
        for (int i = 0; i < 5; i++) {
            buffer.writeLong(i < pressTimestamps.length ? pressTimestamps[i] : 0L);
        }

        buffer.writeLong(cooldownEndTime);
        buffer.writeBoolean(inCooldown);
        buffer.writeBoolean(inRapidClickCooldown);
    }

    public static KeyData decode(FriendlyByteBuf buffer) {
        ResourceLocation keyId = buffer.readResourceLocation();
        KeyState state = buffer.readEnum(KeyState.class);
        float power = buffer.readFloat();
        float remainingTime = buffer.readFloat();
        int rapidClickCount = buffer.readInt();
        long[] pressTimestamps = new long[5];
        for (int i = 0; i < 5; i++) {
            pressTimestamps[i] = buffer.readLong();
        }

        long cooldownEndTime = buffer.readLong();
        boolean inCooldown = buffer.readBoolean();
        boolean inRapidClickCooldown = buffer.readBoolean();

        return new KeyData(keyId, state, power, remainingTime, rapidClickCount,
                pressTimestamps, cooldownEndTime, inCooldown, inRapidClickCooldown);
    }
}