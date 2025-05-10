package org.pickaid.pibrary.content.key;

import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.resources.ResourceLocation;

/**
 * KeyData stores the current state and metadata for a key binding.
 *
 * This class maintains all runtime state information for keys managed by the
 * ClientKeyHandler, including power levels, timing information, and various
 * state flags.
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
     */
    @SerialClass.SerialField
    public float power;

    /**
     * Remaining time until full power (for charging mechanics)
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
        this.state = KeyState.IDLE;
        this.power = 0.0f;
        this.remainingTime = 0.0f;
        this.rapidClickCount = 0;
        this.inRapidClickCooldown = false;
        this.isInCooldown = false;
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

        long remaining = cooldownEndTime - currentTime;
        return Math.max(0, remaining);
    }

    /**
     * Possible states for key bindings.
     *
     * These states form the core of the key binding state machine.
     */
    public enum KeyState {
        IDLE,
        CHARGING,
        PRESSED,
        RELEASED,
        FINISHED,
        RAPID_CLICK,
        RAPID_FINISH,
        TIMEOUT,
        COOLDOWN,
        AWAITING_RELEASE,
        HELD
    }
}