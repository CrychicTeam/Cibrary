package org.pickaid.pibrary.content.key.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

/**
 * ConfiguredKey provides a comprehensive key binding configuration system
 * with support for charging mechanics, rapid click detection, and various
 * timing controls.
 * <p>
 * Core Features:
 * <p>
 * 1. Basic Input - Simple key press/release mechanics with timing controls
 * <p>
 * 2. Power Charging - Allows keys to be held to accumulate power
 * <p>
 * 3. Rapid Click - Detects sequences of quick clicks on the same key
 * <p>
 * 4. Cooldowns - Controls timing between actions to prevent spam
 */
public class ConfiguredKey {
    /** Unique identifier for this key */
    public final ResourceLocation id;

    /** The Minecraft key mapping */
    public final KeyMapping keyMapping;

    // ====== Basic Properties ======

    /** Whether to show debug messages in chat */
    public final boolean showDebugMessage;

    /** Key category for Minecraft's key configuration screen */
    public final String category;

    // ====== Timing Tolerances ======

    /** Minimum time a key must be held to register as a press (ms) */
    public final long pressTimeTolerance;

    // ====== Charging Properties ======

    /** Whether charging mechanics are enabled for this key */
    public final boolean enableCharging;

    /** Maximum power level that can be achieved when charging */
    public final float maxPower;

    /** Whether the key should auto-release when reaching max power */
    public final boolean autoReleaseOnMax;

    /** Minimum time a key must be held before charging begins (ms) */
    public final long chargingTimeTolerance;

    // ====== Rapid Click Properties ======

    /** Whether to track rapid click sequences */
    public final boolean enableRapidClick;

    /** Maximum number of rapid clicks to count in sequence */
    public final int maxRapidClickCount;

    /** Time window in which clicks must occur to be counted as rapid (ms) */
    public final long rapidClickTimeWindow;

    // ====== Cooldown Properties ======

    /** Delay required after physical key release before accepting new input (ms) */
    public final long physicalReleaseDelay;

    /** Cooldown after a standard key press (ms) */
    public final long pressCooldown;

    /** Cooldown after a charged release (ms) */
    public final long releaseCooldown;

    /** Cooldown after a rapid click sequence completes (ms) */
    public final long rapidClickCompleteCooldown;

    /** Cooldown after a rapid click sequence is interrupted or times out (ms) */
    public final long rapidClickResetCooldown;

    public final long timeoutTime;

    /**
     * Private constructor used by Builder.
     * Use {@link Builder} to create instances.
     */
    private ConfiguredKey(Builder builder) {
        this.id = builder.id;

        this.keyMapping = builder.existingKeyMapping != null ?
                builder.existingKeyMapping :
                new KeyMapping(
                        "key." + builder.id.getNamespace() + "." + builder.id.getPath(),
                        builder.type == null ? KeyConflictContext.IN_GAME : builder.type,
                        builder.keyModifier,
                        InputConstants.Type.KEYSYM,
                        builder.defaultKey,
                        "key.category." + builder.id.getNamespace() + "." + builder.category
                );

        this.showDebugMessage = builder.showDebugMessage;
        this.category = builder.category;

        this.pressTimeTolerance = builder.pressTimeTolerance;

        this.enableCharging = builder.enableCharging;
        this.maxPower = builder.maxPower;
        this.autoReleaseOnMax = builder.autoReleaseOnMax;
        this.chargingTimeTolerance = builder.chargingTimeTolerance;

        this.enableRapidClick = builder.enableRapidClick;
        this.maxRapidClickCount = builder.maxRapidClickCount;
        this.rapidClickTimeWindow = builder.rapidClickTimeWindow;

        this.physicalReleaseDelay = builder.physicalReleaseDelay;
        this.pressCooldown = builder.pressCooldown;
        this.releaseCooldown = builder.releaseCooldown;
        this.rapidClickCompleteCooldown = builder.rapidClickCompleteCooldown;
        this.rapidClickResetCooldown = builder.rapidClickResetCooldown;

        this.timeoutTime = builder.timeoutTime;
    }

    public static class Builder {
        private final ResourceLocation id;
        private final int defaultKey;

        private KeyMapping existingKeyMapping = null;
        private KeyConflictContext type = null;
        private String category = "default";
        private KeyModifier keyModifier = KeyModifier.NONE;
        private boolean showDebugMessage = false;

        private long pressTimeTolerance = 50L;

        private boolean enableCharging = false;
        private float maxPower = 2.0f;
        private boolean autoReleaseOnMax = true;
        private long chargingTimeTolerance = 100L;

        private boolean enableRapidClick = false;
        private int maxRapidClickCount = 5;
        private long rapidClickTimeWindow = 300L;

        private long physicalReleaseDelay = 200L;
        private long pressCooldown = 0L;
        private long releaseCooldown = 0L;
        private long rapidClickCompleteCooldown = 0L;
        private long rapidClickResetCooldown = 0L;

        private long timeoutTime = 5000L;

        public Builder(String modid, String keyId, int defaultKey) {
            this.id = new ResourceLocation(modid, keyId);
            this.defaultKey = defaultKey;
        }

        /**
         * Creates a new Builder using an existing KeyMapping.
         *
         * @param modid The mod ID
         * @param keyId The key identifier
         * @param existingKey The existing KeyMapping to use
         */
        public Builder(String modid, String keyId, KeyMapping existingKey) {
            this.id = new ResourceLocation(modid, keyId);
            this.defaultKey = existingKey.getKey().getValue();
            this.existingKeyMapping = existingKey;
        }

        /**
         * Sets the category for this key in Minecraft's key configuration screen.
         * <p>
         * Default: "default"
         *
         * @param category The category name
         * @return This builder for method chaining
         */
        public Builder category(String category) {
            this.category = category;
            return this;
        }

        /**
         * Enables debug messages in the client chat.
         * <p>
         * Default: false
         *
         * @return This builder for method chaining
         */
        public Builder showDebugMessage() {
            this.showDebugMessage = true;
            return this;
        }

        /**
         * Sets the key modifier (Shift, Ctrl, Alt) required with this key.
         * <p>
         * Default: KeyModifier.NONE
         *
         * @param keyModifier The key modifier to use
         * @return This builder for method chaining
         */
        public Builder setKeyModifier(KeyModifier keyModifier) {
            this.keyModifier = keyModifier;
            return this;
        }

        /**
         * Sets the minimum time a key must be held to register as a valid press.
         * <p>
         * This helps filter out accidental or noisy inputs.
         * <p>
         * Default: 50ms
         *
         * @param milliseconds Press time tolerance in milliseconds
         * @return This builder for method chaining
         */
        public Builder setPressTimeTolerance(long milliseconds) {
            this.pressTimeTolerance = milliseconds;
            return this;
        }

        /**
         * Enables charging mechanics for this key.
         * <p>
         * When enabled, holding down the key will accumulate power up to maxPower.
         * <p>
         * Default: false
         *
         * @return This builder for method chaining
         */
        public Builder enableCharging() {
            this.enableCharging = true;
            return this;
        }

        /**
         * Sets the maximum power level for charging, measured in seconds.
         * <p>
         * Power represents the time in seconds the key must be held to reach maximum charge.
         * <p>
         * Default: 2.0f (2 seconds)
         *
         * @param maxPower Power level in seconds (>= 1.0f recommended)
         * @return This builder for method chaining
         */
        public Builder maxPower(float maxPower) {
            this.maxPower = maxPower;
            return this;
        }

        /**
         * Sets whether the key should automatically release when reaching maximum power.
         * <p>
         * When enabled, the key will automatically trigger a release event when
         * reaching maximum power, even if the player is still holding the key.
         * <p>
         * Default: true
         *
         * @param autoRelease Whether to auto-release
         * @return This builder for method chaining
         */
        public Builder autoReleaseOnMax(boolean autoRelease) {
            this.autoReleaseOnMax = autoRelease;
            return this;
        }

        /**
         * Sets the minimum time a key must be held before charging begins.
         * <p>
         * This helps prevent accidental charging when a player intends a normal press.
         * <p>
         * Default: 100ms
         *
         * @param milliseconds Charging time tolerance in milliseconds
         * @return This builder for method chaining
         */
        public Builder setChargingTimeTolerance(long milliseconds) {
            this.chargingTimeTolerance = milliseconds;
            return this;
        }

        // ====== Rapid Click Methods ======

        /**
         * Enables rapid click detection for this key.
         * <p>
         * When enabled, the system will track sequences of quick presses on this key.
         * <p>
         * Default: false
         *
         * @return This builder for method chaining
         */
        public Builder enableRapidClick() {
            this.enableRapidClick = true;
            return this;
        }

        /**
         * Sets the maximum number of rapid clicks that can be counted in sequence.
         * <p>
         * When this limit is reached, the sequence completes and will not count further.
         * <p>
         * Default: 5 clicks
         *
         * @param count Maximum rapid click count
         * @return This builder for method chaining
         */
        public Builder setMaxRapidClickCount(int count) {
            this.maxRapidClickCount = count;
            return this;
        }

        /**
         * Sets the time window in which clicks must occur to be counted as rapid.
         * <p>
         * Clicks outside this window will start a new sequence.
         * <p>
         * Default: 300ms
         *
         * @param milliseconds Time window in milliseconds
         * @return This builder for method chaining
         */
        public Builder setRapidClickTimeWindow(long milliseconds) {
            this.rapidClickTimeWindow = milliseconds;
            return this;
        }

        /**
         * Sets the delay required after physical key release before accepting new input.
         * <p>
         * After auto-release or manual release, the player must physically release the
         * key and wait this amount of time before a new press is recognized.
         * <p>
         * Default: 200ms
         *
         * @param milliseconds Delay in milliseconds
         * @return This builder for method chaining
         */
        public Builder setPhysicalReleaseDelay(long milliseconds) {
            this.physicalReleaseDelay = milliseconds;
            return this;
        }

        /**
         * Sets the cooldown period after a standard key press.
         * <p>
         * During this cooldown, the key will not respond to new inputs.
         * <p>
         * Default: 0ms (no cooldown)
         *
         * @param milliseconds Cooldown duration in milliseconds
         * @return This builder for method chaining
         */
        public Builder setPressCooldown(long milliseconds) {
            this.pressCooldown = milliseconds;
            return this;
        }

        /**
         * Sets the cooldown period specifically after a charged release.
         * <p>
         * This cooldown is applied when a key is released after charging.
         * <p>
         * Default: 0ms (no cooldown)
         *
         * @param milliseconds Cooldown duration in milliseconds
         * @return This builder for method chaining
         */
        public Builder setReleaseCooldown(long milliseconds) {
            this.releaseCooldown = milliseconds;
            return this;
        }

        /**
         * Sets the cooldown period after a rapid click sequence reaches its maximum count.
         * <p>
         * During this cooldown, new rapid click sequences cannot be started.
         * <p>
         * Default: 0ms (no cooldown)
         *
         * @param milliseconds Cooldown duration in milliseconds
         * @return This builder for method chaining
         */
        public Builder setRapidClickCompleteCooldown(long milliseconds) {
            this.rapidClickCompleteCooldown = milliseconds;
            return this;
        }

        /**
         * Sets the cooldown period after a rapid click sequence is interrupted or times out.
         * <p>
         * During this cooldown, new rapid click sequences cannot be started.
         * <p>
         * Default: 0ms (no cooldown)
         *
         * @param milliseconds Cooldown duration in milliseconds
         * @return This builder for method chaining
         */
        public Builder setRapidClickResetCooldown(long milliseconds) {
            this.rapidClickResetCooldown = milliseconds;
            return this;
        }

        public Builder setTimeoutTime(long milliseconds) {
            this.timeoutTime = milliseconds;
            return this;
        }

        /**
         * Builds a new ConfiguredKey with the current settings.
         *
         * @return A new ConfiguredKey instance
         */
        public ConfiguredKey build() {
            return new ConfiguredKey(this);
        }
    }

    /**
     * Checks if this key is currently being pressed.
     *
     * @return true if the key is down, false otherwise
     */
    public boolean isDown() {
        return this.keyMapping.isDown();
    }
}