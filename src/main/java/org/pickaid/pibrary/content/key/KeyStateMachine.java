package org.pickaid.pibrary.content.key;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.registry.KeyConfig;
import org.pickaid.pibrary.content.key.state.*;
import org.pickaid.pibrary.network.PibraryNetworkHandler;
import org.pickaid.pibrary.network.key.KeyStatePacket;

public class KeyStateMachine {
    public static final float POWER_UPDATE_THRESHOLD = 0.1f;
    public static final long CHARGING_UPDATE_INTERVAL = 100L;
    public static final float LEAST_RELEASE_TIME = 0.5f;
    public static final long RAPID_CLICK_THRESHOLD = 300;
    public static final long RAPID_CLICK_INTERVAL = 500;
    public static final long DEFAULT_STATE_TIMEOUT = 5000L;

    private final KeyConfig config;
    private final KeyData keyData;
    private KeyState currentState;

    private boolean previousPressed = false;
    private boolean isCurrentlyPressed = false;
    private long pressStartTime = 0;
    private long heldStartTime = 0;
    private long chargedReleaseTime = 0;
    private long rapidClickCooldownEndTime = 0;
    private long lastNetworkUpdateTime = 0;

    private boolean inTimeoutState = false;
    private boolean pressedTriggered = false;

    private KeyData.KeyState lastSentState = KeyData.KeyState.IDLE;
    private float lastSentPower = 0f;
    private int lastSentRapidCount = 0;
    private boolean updatedThisTick = false;

    public KeyStateMachine(KeyConfig config) {
        this.config = config;
        this.keyData = new KeyData(config.id);
        this.currentState = new IdleState();
    }

    public void resetState() {
        pressedTriggered = false;
        inTimeoutState = false;
        
        if (!isCurrentlyPressed) {
            pressStartTime = 0;
            heldStartTime = 0;
        }
    }

    public void process(boolean isPressed, long currentTime) {
        this.isCurrentlyPressed = isPressed;
        this.updatedThisTick = false;

        KeyData.KeyState oldState = keyData.state;
        int oldRapidCount = keyData.rapidClickCount;
        float oldPower = keyData.power;

        if (keyData.checkCooldown(currentTime)) {
            showDebugMessage(String.format("Cooldown: %d ms remaining",
                    keyData.getRemainingCooldown(currentTime)));
            previousPressed = isPressed;
            return;
        }

        if (rapidClickCooldownEndTime > 0) {
            if (currentTime >= rapidClickCooldownEndTime) {
                rapidClickCooldownEndTime = 0;
                keyData.inRapidClickCooldown = false;
                showDebugMessage("Rapid click cooldown ended");
            } else {
                keyData.inRapidClickCooldown = true;
            }
        }

        KeyState newState = currentState;
        KeyState oldCurrentState = currentState;

        if (isPressed && !previousPressed) {
            showDebugMessage("Key pressed - current state: " + currentState.getClass().getSimpleName());
            pressedTriggered = false;
            newState = currentState.handlePress(this, currentTime);
        } else if (!isPressed && previousPressed) {
            showDebugMessage("Key released - current state: " + currentState.getClass().getSimpleName() + 
                            ", pressTriggered: " + pressedTriggered + 
                            ", pressStartTime: " + pressStartTime);
            
            if (currentState instanceof IdleState && getPressStartTime() > 0 && 
                    !isPressedTriggered() && !getConfig().disableNormalClick) {
                showDebugMessage("Quick press-release detected - moving to PressedState first");
                setPressedTriggered(true);
                currentState = new PressedState();
                keyData.state = currentState.getKeyDataState();
                
                sendNetworkUpdate();
                lastSentState = keyData.state;
                lastNetworkUpdateTime = currentTime;
                updatedThisTick = true;
                showDebugMessage("PRESSED state sent to server (force)");
            }
            
            newState = currentState.handleRelease(this, currentTime);
            
            if (oldCurrentState instanceof PressedState && newState instanceof IdleState) {
                resetState();
            }
        } else {
            newState = currentState.handleTick(this, currentTime);
        }

        if (newState != currentState) {
            showDebugMessage("State changed: " + currentState.getClass().getSimpleName() + 
                            " -> " + newState.getClass().getSimpleName());
            
            KeyState previousState = currentState;
            currentState = newState;
            keyData.state = currentState.getKeyDataState();
            
            boolean isImportantState = currentState instanceof PressedState || 
                                     currentState instanceof ReleasedState ||
                                     currentState instanceof HeldState ||
                                     currentState instanceof ChargingState ||
                                     currentState instanceof FinishedState;
            
            if (keyData.state == KeyData.KeyState.HELD_RELEASED && 
                lastSentState != KeyData.KeyState.HELD_RELEASED && 
                !updatedThisTick) {
                showDebugMessage("CRITICAL STATE DETECTED: HELD_RELEASED - forcing immediate update");
                sendNetworkUpdate();
                lastSentState = keyData.state;
                lastNetworkUpdateTime = currentTime;
                updatedThisTick = true;
            }
            else if (isImportantState && !updatedThisTick) {
                showDebugMessage("Important state change - sending update: " + keyData.state);
                sendNetworkUpdate();
                lastSentState = keyData.state;
                lastNetworkUpdateTime = currentTime;
                updatedThisTick = true;
            }
            
            if (currentState instanceof IdleState && !(previousState instanceof IdleState)) {
                resetState();
            }
        }

        if (!updatedThisTick) {
            checkAndSendNetworkUpdate(oldState, oldRapidCount, oldPower, currentTime);
        }

        previousPressed = isPressed;
    }

    public KeyConfig getConfig() {
        return config;
    }

    public KeyData getKeyData() {
        return keyData;
    }

    public boolean isPressed() {
        return isCurrentlyPressed;
    }

    public void setInTimeoutState(boolean inTimeoutState) {
        this.inTimeoutState = inTimeoutState;
    }

    public boolean isPressedTriggered() {
        return pressedTriggered;
    }

    public void setPressedTriggered(boolean pressedTriggered) {
        this.pressedTriggered = pressedTriggered;
    }

    public long getPressStartTime() {
        return pressStartTime;
    }

    public void setPressStartTime(long pressStartTime) {
        this.pressStartTime = pressStartTime;
    }

    public long getHeldStartTime() {
        return heldStartTime;
    }

    public void setHeldStartTime(long heldStartTime) {
        this.heldStartTime = heldStartTime;
    }

    public long getChargedReleaseTime() {
        return chargedReleaseTime;
    }

    public void setChargedReleaseTime(long chargedReleaseTime) {
        this.chargedReleaseTime = chargedReleaseTime;
    }

    public float getLastSentPower() {
        return lastSentPower;
    }

    public void setLastSentPower(float lastSentPower) {
        this.lastSentPower = lastSentPower;
    }

    public KeyData.KeyState getLastSentState() {
        return lastSentState;
    }

    public void forceLastSentState(KeyData.KeyState state) {
        lastSentState = state;
    }

    public void forceState(KeyData.KeyState state) {
        keyData.state = state;
        lastSentState = state;
        updatedThisTick = true;

        switch (state) {
            case CHARGING:
                currentState = new ChargingState();
                break;
            case HELD:
                currentState = new HeldState();
                break;
            case PRESSED:
                currentState = new PressedState();
                break;
            case RELEASED:
                currentState = new ReleasedState();
                break;
            case RAPID_CLICK:
                currentState = new RapidClickState();
                break;
            case RAPID_FINISH:
                currentState = new RapidFinishState();
                break;
            case TIMEOUT:
                currentState = new TimeoutState();
                inTimeoutState = true;
                break;
            case AWAITING_RELEASE:
                currentState = new AwaitingReleaseState();
                break;
            case FINISHED:
                currentState = new FinishedState();
                break;
            case HELD_RELEASED:
                currentState = new HeldReleasedState();
                break;
            case COOLDOWN:
                currentState = new CooldownState();
                break;
            case IDLE:
            default:
                currentState = new IdleState();
                pressedTriggered = false;
                break;
        }
    }

    public void startRapidClickCooldown(long duration, long currentTime) {
        if (duration <= 0) return;
        keyData.inRapidClickCooldown = true;
        rapidClickCooldownEndTime = currentTime + duration;
    }

    public void triggerRapidClickEvent() {
        keyData.state = KeyData.KeyState.RAPID_CLICK;
        
        sendNetworkUpdate();
        lastSentState = keyData.state;
        lastSentRapidCount = keyData.rapidClickCount;
        updatedThisTick = true;
        showDebugMessage("RAPID_CLICK state sent to server");

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

    public void completeRapidClickSequence(long currentTime) {
        keyData.state = KeyData.KeyState.RAPID_FINISH;
        
        sendNetworkUpdate();
        lastSentState = keyData.state;
        lastSentRapidCount = keyData.rapidClickCount;
        updatedThisTick = true;
        showDebugMessage("RAPID_FINISH state sent to server (RapidClickComplete)");

        if (config.rapidClickCompleteCooldown > 0) {
            startRapidClickCooldown(config.rapidClickCompleteCooldown, currentTime);
            showDebugMessage("Starting rapid click completion cooldown: " +
                    config.rapidClickCompleteCooldown + "ms");
        }

        keyData.rapidClickCount = 0;
        keyData.clearTimestamps();
    }

    public void resetRapidClick(long currentTime) {
        int reachedCount = keyData.rapidClickCount;

        if (reachedCount > 1 && config.rapidClickResetCooldown > 0) {
            startRapidClickCooldown(config.rapidClickResetCooldown, currentTime);
            showDebugMessage("Starting rapid click reset cooldown: " +
                    config.rapidClickResetCooldown + "ms");

            if (config.showDebugMessage) {
                showDebugMessage("Rapid Click Reset (reached " + reachedCount + " clicks)");
            }
        }

        if (reachedCount > 1) {
            keyData.state = KeyData.KeyState.IDLE;
            keyData.rapidClickCount = 0;
            
            if (!updatedThisTick) {
                sendNetworkUpdate();
                lastSentState = keyData.state;
                lastSentRapidCount = 0;
                updatedThisTick = true;
            }
        } else {
            keyData.rapidClickCount = 0;
        }

        keyData.clearTimestamps();
    }

    private void checkAndSendNetworkUpdate(KeyData.KeyState oldState, int oldRapidCount,
                                           float oldPower, long currentTime) {
        if (updatedThisTick) {
            return;
        }

        boolean shouldSendUpdate = false;
        long timeSinceLastUpdate = currentTime - lastNetworkUpdateTime;

        if (oldState != keyData.state) {
            shouldSendUpdate = true;
            
            if ((keyData.state == KeyData.KeyState.PRESSED && lastSentState != KeyData.KeyState.PRESSED) ||
                (keyData.state == KeyData.KeyState.RELEASED && lastSentState != KeyData.KeyState.RELEASED) ||
                (keyData.state == KeyData.KeyState.FINISHED && lastSentState != KeyData.KeyState.FINISHED) ||
                (keyData.state == KeyData.KeyState.RAPID_CLICK && lastSentState != KeyData.KeyState.RAPID_CLICK) ||
                (keyData.state == KeyData.KeyState.RAPID_FINISH && lastSentState != KeyData.KeyState.RAPID_FINISH) ||
                (keyData.state == KeyData.KeyState.HELD_RELEASED && lastSentState != KeyData.KeyState.HELD_RELEASED)) {
                
                showDebugMessage("Critical state change detected: " + keyData.state);
                sendNetworkUpdate();
                lastSentState = keyData.state;
                lastNetworkUpdateTime = currentTime;
                updatedThisTick = true;
                return;
            }
        }
        else if (oldRapidCount != keyData.rapidClickCount) {
            if (keyData.rapidClickCount != lastSentRapidCount) {
                shouldSendUpdate = true;
                lastSentRapidCount = keyData.rapidClickCount;
                showDebugMessage("RapidClick count changed: " + keyData.rapidClickCount);
            }
        }
        else if (keyData.state == KeyData.KeyState.CHARGING || keyData.state == KeyData.KeyState.HELD) {
            boolean powerThresholdReached = Math.abs(keyData.power - lastSentPower) >= POWER_UPDATE_THRESHOLD;
            boolean intervalReached = timeSinceLastUpdate >= CHARGING_UPDATE_INTERVAL;
            
            if (keyData.state == KeyData.KeyState.CHARGING && 
                Math.abs(keyData.power - lastSentPower) < 0.01f && 
                timeSinceLastUpdate < CHARGING_UPDATE_INTERVAL * 3) {
                return;
            }

            if (powerThresholdReached || intervalReached) {
                shouldSendUpdate = true;
                lastSentPower = keyData.power;
            }
        }
        else if (keyData.state == KeyData.KeyState.PRESSED && lastSentState != KeyData.KeyState.PRESSED) {
            shouldSendUpdate = true;
            showDebugMessage("PRESSED state needs to be sent");
        }
        else if (keyData.state == KeyData.KeyState.RAPID_CLICK && lastSentState != KeyData.KeyState.RAPID_CLICK) {
            shouldSendUpdate = true;
            showDebugMessage("RAPID_CLICK state needs to be sent");
        }
        else if (keyData.state == KeyData.KeyState.FINISHED && lastSentState != KeyData.KeyState.FINISHED) {
            shouldSendUpdate = true;
            showDebugMessage("FINISHED state needs to be sent");
        }
        else if (keyData.state == KeyData.KeyState.RAPID_FINISH && lastSentState != KeyData.KeyState.RAPID_FINISH) {
            shouldSendUpdate = true;
            showDebugMessage("RAPID_FINISH state needs to be sent");
        }
        else if (keyData.state == KeyData.KeyState.HELD_RELEASED && lastSentState != KeyData.KeyState.HELD_RELEASED) {
            shouldSendUpdate = true;
            showDebugMessage("HELD_RELEASED state needs to be sent");
        }

        if (shouldSendUpdate) {
            sendNetworkUpdate();
            lastSentState = keyData.state;
            lastNetworkUpdateTime = currentTime;
            updatedThisTick = true;
        }
    }

    public void sendNetworkUpdate() {
        showDebugMessage("Sending network update: " + keyData.state);
        PibraryNetworkHandler.HANDLER.toServer(new KeyStatePacket(keyData));
    }

    public void showDebugMessage(String message) {
        if (config.showDebugMessage) {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(Component.literal(message), true);
                player.sendSystemMessage(Component.literal("Debug Message: " + message));
            }
        }
    }
}