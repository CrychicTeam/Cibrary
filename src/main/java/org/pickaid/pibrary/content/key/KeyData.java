package org.pickaid.pibrary.content.key;

import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.resources.ResourceLocation;

/**
 * KeyData stores the current state and metadata for a key binding.
 *
 * This class maintains all runtime state information for keys managed by the
 * ClientKeyHandler, including power levels, timing information, and various
 * state flags. It provides a more streamlined implementation with cleaner state
 * transitions and optimized member access.
 */
@SerialClass
public class KeyData {
    /**
     * Unique identifier for this key
     */
    @SerialClass.SerialField
    public ResourceLocation keyId;

    /**
     * Current state in the key's state machine
     */
    @SerialClass.SerialField
    public KeyState state;

    /**
     * Current power level (for charging mechanics)
     * This value represents the charge level from 0.0 to maxPower
     */
    @SerialClass.SerialField
    public float power;

    /**
     * Remaining time until full power (for charging mechanics)
     * Calculated as maxPower - power
     */
    @SerialClass.SerialField
    public float remainingTime;

    /**
     * Number of rapid clicks in the current sequence
     */
    @SerialClass.SerialField
    public int rapidClickCount;

    /**
     * Timestamps of the most recent key presses (for rapid click detection)
     * Index 0 contains the most recent press
     */
    @SerialClass.SerialField
    public long[] pressTimestamps = new long[3];

    /**
     * Time when the current cooldown period ends
     */
    @SerialClass.SerialField
    public long cooldownEndTime = 0L;

    /**
     * Whether the key is currently in a general cooldown state
     */
    @SerialClass.SerialField
    public boolean isInCooldown = false;

    /**
     * Whether the key is in rapid click cooldown
     */
    @SerialClass.SerialField
    public boolean inRapidClickCooldown = false;

    /**
     * Last recorded state for network updates
     */
    @SerialClass.SerialField
    public KeyState lastSentState = KeyState.IDLE;

    /**
     * Last recorded power value for network updates
     */
    @SerialClass.SerialField
    public float lastSentPower = 0;

    /**
     * Default constructor. Initializes all state variables to their default values.
     */
    public KeyData() {
        reset();
    }

    /**
     * Creates a new KeyData instance for the specified key.
     *
     * @param keyId The unique identifier for the key
     */
    public KeyData(ResourceLocation keyId) {
        this();
        this.keyId = keyId;
    }

    /**
     * Resets all key data to initial state.
     *
     * This clears all state, power, and timing information, returning
     * the key to its default idle state.
     */
    public void reset() {
        this.state = KeyState.IDLE;
        this.power = 0.0f;
        this.remainingTime = 0.0f;
        this.rapidClickCount = 0;
        this.inRapidClickCooldown = false;
        this.isInCooldown = false;
        // Preserve keyId
    }

    /**
     * Starts the cooldown timer based on the configured duration.
     *
     * During cooldown, the key will not respond to new inputs.
     *
     * @param duration Duration of the cooldown in milliseconds
     */
    public void startCooldown(long duration) {
        if (duration <= 0) return;

        this.cooldownEndTime = System.currentTimeMillis() + duration;
        this.isInCooldown = true;
        this.state = KeyState.COOLDOWN;
    }

    /**
     * Checks if the key is currently in cooldown.
     *
     * Also automatically clears the cooldown flag if the cooldown period has expired.
     *
     * @param currentTime Current system time in milliseconds
     * @return True if the key is still in cooldown, false otherwise
     */
    public boolean checkCooldown(long currentTime) {
        if (!isInCooldown) return false;

        if (currentTime >= cooldownEndTime) {
            isInCooldown = false;
            this.state = KeyState.IDLE;
            return false;
        }

        return true;
    }

    /**
     * Gets the remaining cooldown time in milliseconds.
     *
     * @param currentTime Current system time in milliseconds
     * @return Remaining cooldown time in milliseconds, or 0 if not in cooldown
     */
    public long getRemainingCooldown(long currentTime) {
        if (!isInCooldown) return 0;
        return Math.max(0, cooldownEndTime - currentTime);
    }

    /**
     * Update the power level during charging and calculate remaining time
     *
     * @param currentPower The new power level to set
     * @param maxPower The maximum power level for this key
     */
    public void updatePower(float currentPower, float maxPower) {
        this.power = Math.min(currentPower, maxPower);
        this.remainingTime = maxPower - this.power;
    }

    /**
     * Records a new key press timestamp and shifts older timestamps
     *
     * @param timestamp The timestamp to record
     */
    public void recordPressTimestamp(long timestamp) {
        System.arraycopy(pressTimestamps, 0, pressTimestamps, 1, pressTimestamps.length - 1);
        pressTimestamps[0] = timestamp;
    }

    /**
     * Clears all timestamp data
     */
    public void clearTimestamps() {
        for (int i = 0; i < pressTimestamps.length; i++) {
            pressTimestamps[i] = 0;
        }
    }

    /**
     * Checks if this key has network updates to send
     *
     * @param currentTime Current timestamp
     * @param updateInterval Minimum time between updates
     * @param powerThreshold Minimum power change to trigger update
     * @return True if updates should be sent
     */
    public boolean hasNetworkUpdates(long currentTime, long lastUpdateTime,
                                     long updateInterval, float powerThreshold) {
        // State change always triggers update
        if (state != lastSentState) return true;

        // Power change exceeds threshold
        if (Math.abs(power - lastSentPower) >= powerThreshold) return true;

        // Regular update interval for charging/held states
        if ((state == KeyState.CHARGING || state == KeyState.HELD) &&
                (currentTime - lastUpdateTime) >= updateInterval) {
            return true;
        }

        return false;
    }

    /**
     * Possible states for key bindings.
     *
     * These states form the core of the key binding state machine.
     */
    public enum KeyState {
        IDLE,              // Key is not pressed or in use
        CHARGING,          // Key is being held down to accumulate power
        PRESSED,           // Key has been pressed once
        RELEASED,          // Key has been released after being pressed before reaching max power
        FINISHED,          // An action has completed automatically (e.g., auto-release at max power)
        HELD,              // Key is being held at max power (when auto-release is false)
        HELD_RELEASED,     // Key was manually released from HELD state
        RAPID_CLICK,       // Key is being rapidly clicked
        RAPID_FINISH,      // A rapid click sequence has completed
        TIMEOUT,           // Key has been held too long and timed out
        COOLDOWN,          // Key is in cooldown period after use
        AWAITING_RELEASE   // Key is waiting for physical release after an action
    }
}