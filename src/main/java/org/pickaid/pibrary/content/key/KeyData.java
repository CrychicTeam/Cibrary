package org.pickaid.pibrary.content.key;

import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.resources.ResourceLocation;

/**
 * Data class for key states, used for network synchronization and state management.
 * This class is designed to work with the state machine pattern implementation.
 */
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

    @SerialClass.SerialField
    public int rapidClickCount;

    @SerialClass.SerialField
    public long[] pressTimestamps = new long[5];

    @SerialClass.SerialField
    public long cooldownEndTime = 0L;

    @SerialClass.SerialField
    public boolean inCooldown = false;

    @SerialClass.SerialField
    public boolean inRapidClickCooldown = false;

    /**
     * Default constructor for serialization
     */
    public KeyData() {
        reset();
    }

    /**
     * Constructor with key ID
     * @param keyId Resource location ID for the key
     */
    public KeyData(ResourceLocation keyId) {
        this();
        this.keyId = keyId;
    }

    /**
     * Reset all state values to defaults
     */
    public void reset() {
        state = KeyState.IDLE;
        power = 0.0f;
        remainingTime = 0.0f;
        rapidClickCount = 0;
        inRapidClickCooldown = false;
        inCooldown = false;
        pressTimestamps = new long[5];
    }

    /**
     * All possible key states in the state machine
     */
    public enum KeyState {
        /** No key interaction */
        IDLE,

        /** Key is being held and charging power */
        CHARGING,

        /** Key was pressed and released in valid press time */
        PRESSED,

        /** Key was released during charging */
        RELEASED,

        /** Key reached max charge and auto-released */
        FINISHED,

        /** Key reached max charge and is still held */
        HELD,

        /** Key was in HELD state and then released */
        HELD_RELEASED,

        /** Key is part of a rapid click sequence */
        RAPID_CLICK,

        /** Rapid click sequence completed successfully */
        RAPID_FINISH,

        /** Key was held too long and timed out */
        TIMEOUT,

        /** Key is in cooldown period after an action */
        COOLDOWN,

        /** Key is waiting for physical release after auto-release */
        AWAITING_RELEASE
    }

    /**
     * Creates a deep copy of this KeyData instance
     * @return A new KeyData instance with the same values
     */
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

    /**
     * Starts a cooldown period for this key
     *
     * @param duration Duration of the cooldown in milliseconds
     */
    public void startCooldown(long duration) {
        if (duration <= 0) return;

        inCooldown = true;
        cooldownEndTime = System.currentTimeMillis() + duration;
        state = KeyState.COOLDOWN;
    }

    /**
     * Checks if the key is still in cooldown
     *
     * @param currentTime Current system time in milliseconds
     * @return true if still in cooldown, false otherwise
     */
    public boolean checkCooldown(long currentTime) {
        if (!inCooldown) return false;

        if (currentTime >= cooldownEndTime) {
            inCooldown = false;
            cooldownEndTime = 0;
            return false;
        }

        return true;
    }

    /**
     * Gets the remaining cooldown time
     *
     * @param currentTime Current system time in milliseconds
     * @return Remaining cooldown time in milliseconds
     */
    public long getRemainingCooldown(long currentTime) {
        if (!inCooldown || cooldownEndTime <= currentTime) return 0;
        return cooldownEndTime - currentTime;
    }

    /**
     * Updates the power value for charging keys
     *
     * @param newPower New power value
     * @param maxPower Maximum allowed power
     */
    public void updatePower(float newPower, float maxPower) {
        power = Math.min(newPower, maxPower);
    }

    /**
     * Records a timestamp for rapid click detection
     *
     * @param timestamp Timestamp to record
     */
    public void recordPressTimestamp(long timestamp) {
        // Shift all existing timestamps down
        if (pressTimestamps.length > 1) {
            System.arraycopy(pressTimestamps, 0, pressTimestamps, 1, pressTimestamps.length - 1);
        }

        // Add new timestamp at index 0
        pressTimestamps[0] = timestamp;
    }

    /**
     * Clears all recorded timestamps
     */
    public void clearTimestamps() {
        for (int i = 0; i < pressTimestamps.length; i++) {
            pressTimestamps[i] = 0;
        }
    }

    /**
     * @return A human-readable representation of this key's state
     */
    @Override
    public String toString() {
        return String.format("KeyData[%s, state=%s, power=%.2f, clicks=%d]",
                keyId, state, power, rapidClickCount);
    }
}