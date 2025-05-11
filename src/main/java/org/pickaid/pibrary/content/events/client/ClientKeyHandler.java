package org.pickaid.pibrary.content.events.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.key.registry.ConfiguredKey;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.registry.KeyRegistry;
import org.pickaid.pibrary.network.PibraryNetworkHandler;
import org.pickaid.pibrary.network.key.KeyStatePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ClientKeyHandler manages all custom key inputs on the client side.
 *
 * This class provides a sophisticated input handling system with support for:
 * - Key state tracking (press, release, hold)
 * - Power charging mechanics (hold to build power)
 * - Rapid click detection
 * - Customizable cooldowns and delays
 * - Network synchronization
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class ClientKeyHandler {
    public static final Map<ResourceLocation, KeyData> KEY_STATES = new HashMap<>();
    public static final Map<ResourceLocation, Boolean> PREVIOUS_STATES = new HashMap<>();
    public static final Map<ResourceLocation, Long> PRESS_START_TIMES = new HashMap<>();
    public static final Map<ResourceLocation, KeyData.KeyState> LAST_SENT_STATE = new HashMap<>();

    private static final Map<ResourceLocation, Long> CHARGED_RELEASE_TIMES = new HashMap<>();
    private static final Map<ResourceLocation, Boolean> AUTO_RELEASED = new HashMap<>();
    private static final Map<ResourceLocation, Boolean> RELEASE_BLOCK_INPUT = new HashMap<>();
    private static final Map<ResourceLocation, Long> RAPID_CLICK_COOLDOWN_TIMES = new HashMap<>();
    private static final Map<ResourceLocation, Boolean> IN_TIMEOUT_STATE = new HashMap<>();
    private static final Map<ResourceLocation, Long> HELD_START_TIMES = new HashMap<>();
    private static final Map<ResourceLocation, Boolean> PRESSED_TRIGGERED = new HashMap<>();

    private static final Map<ResourceLocation, Float> LAST_SENT_POWER = new HashMap<>();
    private static final Map<ResourceLocation, Integer> LAST_SENT_RAPID_COUNT = new HashMap<>();
    private static final Map<ResourceLocation, Long> LAST_NETWORK_UPDATE = new HashMap<>();

    private static final float POWER_UPDATE_THRESHOLD = 0.1f;
    private static final long CHARGING_UPDATE_INTERVAL = 100L;
    public static final float LEAST_RELEASE_TIME = 0.5f;
    public static final long RAPID_CLICK_THRESHOLD = 300;
    public static final long RAPID_CLICK_INTERVAL = 500;
    private static final long DEFAULT_STATE_TIMEOUT = 5000L;

    /**
     * Main tick handler for client-side key processing.
     * Processes all registered keys and updates their states based on user input.
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != Phase.END || Minecraft.getInstance().player == null) return;
        long currentTime = System.currentTimeMillis();

        for (ConfiguredKey config : KeyRegistry.getAllConfigs()) {
            ResourceLocation keyId = config.id;
            KeyData keyData = KEY_STATES.get(keyId);
            Boolean prevPressed = PREVIOUS_STATES.get(keyId);

            if (keyData == null || prevPressed == null) {
                Pibrary.LOGGER.error("Missing key data for registered key: {}", keyId);
                continue;
            }

            if (keyData.checkCooldown(currentTime)) {
                showDebugMessage(String.format("Cooldown: %d ms remaining",
                        keyData.getRemainingCooldown(currentTime)), config);
                PREVIOUS_STATES.put(keyId, config.isDown());
                continue;
            }

            processRapidClickCooldown(keyId, keyData, currentTime, config);

            boolean isCurrentlyDown = config.isDown();

            if (processBlockedInput(keyId, keyData, isCurrentlyDown, prevPressed, currentTime, config)) {
                PREVIOUS_STATES.put(keyId, isCurrentlyDown);
                continue;
            }

            if (processReleaseDelay(keyId, keyData, currentTime, config)) {
                PREVIOUS_STATES.put(keyId, isCurrentlyDown);
                continue;
            }

            KeyData.KeyState oldState = keyData.state;
            int oldRapidCount = keyData.rapidClickCount;
            float oldPower = keyData.power;

            updateKeyState(keyId, config, keyData, isCurrentlyDown, prevPressed, currentTime);
            checkAndSendNetworkUpdate(keyId, keyData, oldState, oldRapidCount, oldPower, currentTime);

            PREVIOUS_STATES.put(keyId, isCurrentlyDown);
            if (keyData.state != KeyData.KeyState.IDLE &&
                    keyData.state != KeyData.KeyState.CHARGING &&
                    keyData.state != KeyData.KeyState.COOLDOWN &&
                    keyData.state != KeyData.KeyState.AWAITING_RELEASE &&
                    keyData.state != KeyData.KeyState.TIMEOUT &&
                    keyData.state != KeyData.KeyState.HELD) {
                keyData.state = KeyData.KeyState.IDLE;
            }
        }
    }

    /**
     * Processes the rapid click cooldown state.
     *
     * @param keyId The key identifier
     * @param keyData The key's state data
     * @param currentTime Current system time
     * @param config The key's configuration
     */
    private static void processRapidClickCooldown(ResourceLocation keyId, KeyData keyData,
                                                  long currentTime, ConfiguredKey config) {
        Long rapidClickCooldownEnd = RAPID_CLICK_COOLDOWN_TIMES.get(keyId);
        if (rapidClickCooldownEnd != null) {
            if (currentTime >= rapidClickCooldownEnd) {
                RAPID_CLICK_COOLDOWN_TIMES.remove(keyId);
                keyData.inRapidClickCooldown = false;
                showDebugMessage("Rapid click cooldown ended: " + keyId.getPath(), config);
            } else {
                keyData.inRapidClickCooldown = true;
            }
        }
    }

    /**
     * Processes keys that are blocking input until physical release.
     *
     * @return true if input is blocked, false otherwise
     */
    private static boolean processBlockedInput(ResourceLocation keyId, KeyData keyData,
                                               boolean isCurrentlyDown, boolean prevPressed,
                                               long currentTime, ConfiguredKey config) {
        Boolean blockingInput = RELEASE_BLOCK_INPUT.getOrDefault(keyId, false);
        if (!blockingInput) return false;

        if (!isCurrentlyDown && prevPressed) {
            showDebugMessage("Physical release after auto-release - unblocking input: " + keyId.getPath(), config);
            RELEASE_BLOCK_INPUT.put(keyId, false);
            AUTO_RELEASED.put(keyId, false);
            if (config.physicalReleaseDelay > 0) {
                CHARGED_RELEASE_TIMES.put(keyId, currentTime);
                keyData.state = KeyData.KeyState.AWAITING_RELEASE;
                showDebugMessage("Starting release delay: " + config.physicalReleaseDelay + "ms", config);
            } else {
                keyData.state = KeyData.KeyState.IDLE;
            }

            sendKeyState(keyData);
            LAST_SENT_STATE.put(keyId, keyData.state);
        } else {
            showDebugMessage("Blocking input until physical release: " + keyId.getPath(), config);
        }

        return true;
    }

    /**
     * Processes keys in the release delay state.
     *
     * @return true if in release delay, false otherwise
     */
    private static boolean processReleaseDelay(ResourceLocation keyId, KeyData keyData,
                                               long currentTime, ConfiguredKey config) {
        if (keyData.state != KeyData.KeyState.AWAITING_RELEASE) return false;

        Long releaseTime = CHARGED_RELEASE_TIMES.get(keyId);
        if (releaseTime == null) {
            keyData.state = KeyData.KeyState.IDLE;
            return false;
        }

        long elapsedSinceRelease = currentTime - releaseTime;
        if (elapsedSinceRelease < config.physicalReleaseDelay) {
            long remaining = config.physicalReleaseDelay - elapsedSinceRelease;
            showDebugMessage(String.format("Release delay: %d ms remaining", remaining), config);
            return true;
        } else {
            keyData.state = KeyData.KeyState.IDLE;
            CHARGED_RELEASE_TIMES.remove(keyId);
            showDebugMessage("Release delay completed: " + keyId.getPath(), config);

            sendKeyState(keyData);
            LAST_SENT_STATE.put(keyId, keyData.state);
            return false;
        }
    }

    /**
     * Checks if a network update is needed and sends it if necessary.
     * This optimizes network traffic by only sending updates on significant changes.
     */
    private static void checkAndSendNetworkUpdate(ResourceLocation keyId, KeyData keyData,
                                                  KeyData.KeyState oldState, int oldRapidCount,
                                                  float oldPower, long currentTime) {
        boolean shouldSendUpdate = false;
        long lastUpdate = LAST_NETWORK_UPDATE.getOrDefault(keyId, 0L);
        long timeSinceLastUpdate = currentTime - lastUpdate;

        if (oldState != keyData.state) {
            shouldSendUpdate = true;
        }
        else if (oldRapidCount != keyData.rapidClickCount) {
            int lastSentCount = LAST_SENT_RAPID_COUNT.getOrDefault(keyId, 0);
            if (keyData.rapidClickCount != lastSentCount) {
                shouldSendUpdate = true;
                LAST_SENT_RAPID_COUNT.put(keyId, keyData.rapidClickCount);
            }
        }
        else if (keyData.state == KeyData.KeyState.CHARGING || keyData.state == KeyData.KeyState.HELD) {
            float lastSentPower = LAST_SENT_POWER.getOrDefault(keyId, 0f);
            boolean powerThresholdReached = Math.abs(keyData.power - lastSentPower) >= POWER_UPDATE_THRESHOLD;
            boolean intervalReached = timeSinceLastUpdate >= CHARGING_UPDATE_INTERVAL;

            if (powerThresholdReached || intervalReached) {
                shouldSendUpdate = true;
                LAST_SENT_POWER.put(keyId, keyData.power);
            }
        }

        if (shouldSendUpdate) {
            sendKeyState(keyData);
            LAST_SENT_STATE.put(keyId, keyData.state);
            LAST_NETWORK_UPDATE.put(keyId, currentTime);
        }
    }

    /**
     * Core method for updating key state based on current input.
     * Manages transitions between different key states.
     */
    private static void updateKeyState(ResourceLocation keyId, ConfiguredKey config, KeyData keyData,
                                       boolean isPressed, boolean prevPressed, long currentTime) {
        boolean inActiveRapidClick = keyData.rapidClickCount > 0 &&
                !keyData.inRapidClickCooldown && config.enableRapidClick;

        boolean isInTimeout = IN_TIMEOUT_STATE.getOrDefault(keyId, false);
        if (isInTimeout) {
            if (isPressed) {
                if (keyData.state != KeyData.KeyState.TIMEOUT) {
                    keyData.state = KeyData.KeyState.TIMEOUT;
                }
                return;
            } else {
                IN_TIMEOUT_STATE.put(keyId, false);
                HELD_START_TIMES.remove(keyId);
                PRESSED_TRIGGERED.remove(keyId);
                keyData.state = KeyData.KeyState.IDLE;
                PRESS_START_TIMES.put(keyId, 0L);
                return;
            }
        }

        if (isPressed) {
            if (!prevPressed) {
                handleInitialKeyPress(keyId, config, keyData, currentTime);
                return;
            }
            if (keyData.state == KeyData.KeyState.HELD) {
                checkHeldTimeout(keyId, config, keyData, currentTime);
                return;
            }
            processContinuingPress(keyId, config, keyData, currentTime);
        }
        else if (prevPressed && !inActiveRapidClick) {
            handleKeyRelease(keyId, config, keyData, currentTime);
        }

        if (config.enableRapidClick && keyData.rapidClickCount > 0 && keyData.pressTimestamps[0] > 0 &&
                (currentTime - keyData.pressTimestamps[0]) > RAPID_CLICK_INTERVAL) {
            if (keyData.rapidClickCount == 1 && !PRESSED_TRIGGERED.getOrDefault(keyId, false)) {
                keyData.state = KeyData.KeyState.PRESSED;
                PRESSED_TRIGGERED.put(keyId, true);
                showDebugMessage("Key Pressed (after rapid click timeout): " + keyId.getPath(), config);
                sendKeyState(keyData);
            }

            resetRapidClick(keyId, keyData, config, currentTime);
            showDebugMessage("Rapid Click Reset (timeout): " + keyId.getPath(), config);
        }
    }

    /**
     * Processes a continuing key press (key was already pressed in previous tick).
     */
    private static void processContinuingPress(ResourceLocation keyId, ConfiguredKey config,
                                               KeyData keyData, long currentTime) {
        long holdDuration = currentTime - PRESS_START_TIMES.getOrDefault(keyId, currentTime);

        if (config.enableRapidClick && keyData.rapidClickCount == 1 &&
                keyData.state == KeyData.KeyState.IDLE) {
            boolean pressedTriggered = PRESSED_TRIGGERED.getOrDefault(keyId, false);
            if (!pressedTriggered && holdDuration > config.pressTimeTolerance) {
                keyData.state = KeyData.KeyState.PRESSED;
                PRESSED_TRIGGERED.put(keyId, true);
                showDebugMessage("Key Pressed (during potential rapid click): " + keyId.getPath(), config);
            }
        }

        if (holdDuration > config.chargingTimeTolerance &&
                config.enableCharging &&
                keyData.state == KeyData.KeyState.IDLE &&
                keyData.rapidClickCount == 0) {
            keyData.state = KeyData.KeyState.CHARGING;
            showDebugMessage("Start Charging: " + keyId.getPath(), config);
            LAST_SENT_POWER.put(keyId, 0f);
        }
        if (keyData.state == KeyData.KeyState.CHARGING) {
            updateChargePower(keyId, config, keyData, currentTime);
        }

        if (config.enableRapidClick && holdDuration > RAPID_CLICK_INTERVAL &&
                keyData.rapidClickCount > 0) {
            if (keyData.rapidClickCount == 1 && !PRESSED_TRIGGERED.getOrDefault(keyId, false)) {
                keyData.state = KeyData.KeyState.PRESSED;
                PRESSED_TRIGGERED.put(keyId, true);
                showDebugMessage("Key Pressed (after rapid click timeout): " + keyId.getPath(), config);
                sendKeyState(keyData);
            }

            resetRapidClick(keyId, keyData, config, currentTime);
            showDebugMessage("Rapid Click Reset (holding too long): " + keyId.getPath(), config);
        }
        if (keyData.state == KeyData.KeyState.CHARGING) {
            checkChargingTimeout(keyId, config, keyData, currentTime);
        }
    }

    /**
     * Checks if a key in charging state has reached its timeout duration.
     */
    private static void checkChargingTimeout(ResourceLocation keyId, ConfiguredKey config,
                                             KeyData keyData, long currentTime) {
        long timeoutDuration = config.timeoutTime > 0 ? config.timeoutTime : DEFAULT_STATE_TIMEOUT;
        long holdDuration = currentTime - PRESS_START_TIMES.getOrDefault(keyId, currentTime);

        if (holdDuration > timeoutDuration) {
            keyData.state = KeyData.KeyState.TIMEOUT;
            IN_TIMEOUT_STATE.put(keyId, true);
            showDebugMessage("Charge Timeout", config);
            sendKeyState(keyData);
            LAST_SENT_STATE.put(keyId, keyData.state);
        }
    }

    /**
     * Checks if a key in held state has reached its timeout duration.
     */
    private static void checkHeldTimeout(ResourceLocation keyId, ConfiguredKey config,
                                         KeyData keyData, long currentTime) {
        long timeoutDuration = config.timeoutTime > 0 ? config.timeoutTime : DEFAULT_STATE_TIMEOUT;

        Long heldStartTime = HELD_START_TIMES.get(keyId);
        if (heldStartTime != null) {
            long heldDuration = currentTime - heldStartTime;
            if (heldDuration > timeoutDuration) {
                keyData.state = KeyData.KeyState.TIMEOUT;
                IN_TIMEOUT_STATE.put(keyId, true);
                showDebugMessage(String.format("Held Timeout after %d ms", heldDuration), config);
                sendKeyState(keyData);
                LAST_SENT_STATE.put(keyId, keyData.state);
            }
        }
    }

    /**
     * Handles the initial key press events and rapid click detection.
     */
    private static void handleInitialKeyPress(ResourceLocation keyId, ConfiguredKey config,
                                              KeyData keyData, long currentTime) {
        PRESS_START_TIMES.put(keyId, currentTime);
        PRESSED_TRIGGERED.put(keyId, false);

        if (config.enableRapidClick) {
            keyData.recordPressTimestamp(currentTime);

            if (keyData.pressTimestamps[1] > 0 && !keyData.inRapidClickCooldown) {
                long timeSincePreviousClick = currentTime - keyData.pressTimestamps[1];

                if (timeSincePreviousClick < RAPID_CLICK_THRESHOLD) {
                    keyData.rapidClickCount++;
                    int maxCount = config.maxRapidClickCount > 0 ? config.maxRapidClickCount : 5;
                    if (keyData.rapidClickCount >= maxCount) {
                        showDebugMessage("Max Rapid Clicks Reached! (" + maxCount + "): " + keyId.getPath(), config);
                        completeRapidClickSequence(keyId, keyData, config, currentTime);
                    } else {
                        showDebugMessage("Rapid Click #" + keyData.rapidClickCount + ": " + keyId.getPath(), config);
                        triggerRapidClickEvent(keyData, config);
                    }
                } else {
                    keyData.rapidClickCount = 1;
                    showDebugMessage("New Click Sequence: " + keyId.getPath(), config);
                }
            } else if (!keyData.inRapidClickCooldown) {
                keyData.rapidClickCount = 1;
                showDebugMessage("First Click: " + keyId.getPath(), config);
            } else {
                showDebugMessage("Click during cooldown ignored: " + keyId.getPath(), config);
            }
        } else {
            keyData.rapidClickCount = 0;
            keyData.clearTimestamps();
        }
    }

    /**
     * Handles key release events.
     */
    private static void handleKeyRelease(ResourceLocation keyId, ConfiguredKey config,
                                         KeyData keyData, long currentTime) {
        long holdDuration = currentTime - PRESS_START_TIMES.getOrDefault(keyId, currentTime);

        if (keyData.state == KeyData.KeyState.TIMEOUT) {
            keyData.state = KeyData.KeyState.IDLE;
            showDebugMessage("Key released from timeout state: " + keyId.getPath(), config);
            PRESS_START_TIMES.put(keyId, 0L);
            IN_TIMEOUT_STATE.put(keyId, false);
            HELD_START_TIMES.remove(keyId);
            PRESSED_TRIGGERED.remove(keyId);
            return;
        }

        if (keyData.state == KeyData.KeyState.HELD) {
            keyData.state = KeyData.KeyState.HELD_RELEASED;
            showDebugMessage(String.format("Manual release from HELD state - Power: %.1f", keyData.power), config);

            if (config.releaseCooldown > 0) {
                keyData.startCooldown(config.releaseCooldown);
                showDebugMessage("Started release cooldown: " + config.releaseCooldown + "ms", config);
            }
            if (config.physicalReleaseDelay > 0) {
                CHARGED_RELEASE_TIMES.put(keyId, currentTime);
                keyData.state = KeyData.KeyState.AWAITING_RELEASE;
                showDebugMessage("Starting release delay: " + config.physicalReleaseDelay + "ms", config);
            }

            HELD_START_TIMES.remove(keyId);
            PRESS_START_TIMES.put(keyId, 0L);
            PRESSED_TRIGGERED.remove(keyId);
            return;
        }

        if (keyData.state == KeyData.KeyState.CHARGING) {
            boolean isAutoRelease = keyData.power >= config.maxPower && config.autoReleaseOnMax;
            boolean hasEnoughPower = keyData.power >= LEAST_RELEASE_TIME;

            if (isAutoRelease || hasEnoughPower) {
                keyData.state = KeyData.KeyState.RELEASED;

                if (config.releaseCooldown > 0) {
                    keyData.startCooldown(config.releaseCooldown);
                    showDebugMessage("Started release cooldown: " + config.releaseCooldown + "ms", config);
                }

                if (isAutoRelease) {
                    AUTO_RELEASED.put(keyId, true);
                    RELEASE_BLOCK_INPUT.put(keyId, true);
                    showDebugMessage("Auto Released (blocking next input): " + keyId.getPath(), config);
                } else {
                    showDebugMessage(String.format("Released %s - Power: %.1f", keyId.getPath(), keyData.power), config);

                    if (config.physicalReleaseDelay > 0) {
                        CHARGED_RELEASE_TIMES.put(keyId, currentTime);
                        keyData.state = KeyData.KeyState.AWAITING_RELEASE;
                        showDebugMessage("Starting release delay: " + config.physicalReleaseDelay + "ms", config);
                    }
                }
            } else if (holdDuration >= config.pressTimeTolerance) {
                keyData.state = KeyData.KeyState.RELEASED;
                showDebugMessage("Key Released (insufficient power): " + keyId.getPath(), config);
            } else {
                keyData.state = KeyData.KeyState.IDLE;
                showDebugMessage("Ignored too short press: " + keyId.getPath(), config);
            }
        }
        else if (holdDuration >= config.pressTimeTolerance) {
            keyData.state = KeyData.KeyState.PRESSED;
            showDebugMessage("Key Pressed: " + keyId.getPath(), config);

            if (config.pressCooldown > 0) {
                keyData.startCooldown(config.pressCooldown);
                showDebugMessage("Started press cooldown: " + config.pressCooldown + "ms", config);
            }
        } else {
            keyData.state = KeyData.KeyState.IDLE;
            showDebugMessage("Ignored too short press: " + keyId.getPath(), config);
        }

        PRESS_START_TIMES.put(keyId, 0L);
        HELD_START_TIMES.remove(keyId);
        PRESSED_TRIGGERED.remove(keyId);
    }

    /**
     * Updates the charge power for a key in charging state.
     */
    private static void updateChargePower(ResourceLocation keyId, ConfiguredKey config,
                                          KeyData keyData, long currentTime) {
        long holdDuration = currentTime - PRESS_START_TIMES.getOrDefault(keyId, currentTime);
        float newPower = Math.min((float) holdDuration / 1000.0f, config.maxPower);
        keyData.updatePower(newPower, config.maxPower);
        if (keyData.power >= config.maxPower) {
            if (config.autoReleaseOnMax) {
                keyData.state = KeyData.KeyState.FINISHED;
                config.keyMapping.release();
                AUTO_RELEASED.put(keyId, true);
                RELEASE_BLOCK_INPUT.put(keyId, true);

                if (config.releaseCooldown > 0) {
                    keyData.startCooldown(config.releaseCooldown);
                    showDebugMessage("Started release cooldown: " + config.releaseCooldown + "ms", config);
                }
                showDebugMessage("Auto Finished (blocking next input): " + keyId.getPath(), config);
                PRESS_START_TIMES.put(keyId, 0L);
                HELD_START_TIMES.remove(keyId);
            } else {
                if (keyData.state != KeyData.KeyState.HELD) {
                    keyData.state = KeyData.KeyState.HELD;
                    HELD_START_TIMES.put(keyId, currentTime);
                    showDebugMessage("Max Charge Reached - Entering HELD state: " + keyId.getPath(), config);
                }
            }
        }
    }

    /**
     * Triggers a rapid click event and sends the update to the server.
     */
    private static void triggerRapidClickEvent(KeyData keyData, ConfiguredKey config) {
        keyData.state = KeyData.KeyState.RAPID_CLICK;
        sendKeyState(keyData);
        LAST_SENT_STATE.put(keyData.keyId, keyData.state);
        LAST_SENT_RAPID_COUNT.put(keyData.keyId, keyData.rapidClickCount);

        if (config.showDebugMessage) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                        Component.literal("Rapid Click x" + keyData.rapidClickCount),
                        true
                );
            }
        }
    }

    /**
     * Completes a rapid click sequence when the maximum count is reached.
     */
    private static void completeRapidClickSequence(ResourceLocation keyId, KeyData keyData,
                                                   ConfiguredKey config, long currentTime) {
        keyData.state = KeyData.KeyState.FINISHED;
        sendKeyState(keyData);
        LAST_SENT_STATE.put(keyId, keyData.state);
        LAST_SENT_RAPID_COUNT.put(keyId, keyData.rapidClickCount);

        if (config.rapidClickCompleteCooldown > 0) {
            startRapidClickCooldown(keyId, config.rapidClickCompleteCooldown, currentTime);
            showDebugMessage("Starting rapid click completion cooldown: " +
                    config.rapidClickCompleteCooldown + "ms", config);
        }

        keyData.rapidClickCount = 0;
        keyData.clearTimestamps();
    }

    /**
     * Resets a rapid click sequence that was interrupted or timed out.
     */
    private static void resetRapidClick(ResourceLocation keyId, KeyData keyData,
                                        ConfiguredKey config, long currentTime) {
        int reachedCount = keyData.rapidClickCount;

        if (reachedCount > 1 && config.rapidClickResetCooldown > 0) {
            startRapidClickCooldown(keyId, config.rapidClickResetCooldown, currentTime);
            showDebugMessage("Starting rapid click reset cooldown: " +
                    config.rapidClickResetCooldown + "ms", config);

            if (config.showDebugMessage) {
                showDebugMessage("Rapid Click Reset: " + keyId.getPath() +
                        " (reached " + reachedCount + " clicks)", config);
            }
        }

        if (reachedCount > 1) {
            keyData.state = KeyData.KeyState.IDLE;
            keyData.rapidClickCount = 0;
            sendKeyState(keyData);
            LAST_SENT_STATE.put(keyId, keyData.state);
            LAST_SENT_RAPID_COUNT.put(keyId, 0);
        } else {
            keyData.rapidClickCount = 0;
        }

        keyData.clearTimestamps();
    }

    private static void startRapidClickCooldown(ResourceLocation keyId, long duration, long currentTime) {
        if (duration <= 0) return;

        KeyData keyData = KEY_STATES.get(keyId);
        if (keyData != null) {
            keyData.inRapidClickCooldown = true;
            RAPID_CLICK_COOLDOWN_TIMES.put(keyId, currentTime + duration);
        }
    }

    private static void sendKeyState(KeyData keyData) {
        PibraryNetworkHandler.HANDLER.toServer(new KeyStatePacket(keyData));
    }

    private static void showDebugMessage(String message, ConfiguredKey config) {
        if (config.showDebugMessage) {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(Component.literal(message), true);
                player.sendSystemMessage(Component.literal("Debug Message: " + message));
            }
        }
    }

    /**
     * Gets the current state of a key.
     *
     * @param keyId The key identifier
     * @return An Optional containing the key's state data, or empty if not found
     */
    public static Optional<KeyData> getKeyState(ResourceLocation keyId) {
        return Optional.ofNullable(KEY_STATES.get(keyId));
    }

    /**
     * Sets the current state of a key.
     *
     * @param keyData The key's state data to set
     */
    public static void setKeState(KeyData keyData) {
        var existingKeyData = KEY_STATES.get(keyData.keyId);
        if (existingKeyData != null) {
            existingKeyData.state = keyData.state;
            LAST_SENT_STATE.put(keyData.keyId, keyData.state);
            sendKeyState(keyData);
        }
    }
}