package org.pickaid.pibrary.content.key;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.network.NetworkSyncManager;
import org.pickaid.pibrary.content.key.registry.KeyConfig;
import org.pickaid.pibrary.content.key.state.*;
import org.pickaid.pibrary.content.key.timing.KeyTimingTracker;

public class KeyStateMachine {
    public static final float POWER_UPDATE_THRESHOLD = 0.1f;
    public static final long CHARGING_UPDATE_INTERVAL = 100L;
    public static final float LEAST_RELEASE_TIME = 0.5f;
    public static final long DEFAULT_STATE_TIMEOUT = 5000L;

    private final KeyConfig config;
    private final KeyData keyData;
    private KeyState currentState;

    private final NetworkSyncManager networkManager;
    private final KeyTimingTracker timingTracker;

    public KeyStateMachine(KeyConfig config) {
        this.config = config;
        this.keyData = new KeyData(config.id);
        this.currentState = new IdleState();
        this.networkManager = new NetworkSyncManager(this);
        this.timingTracker = new KeyTimingTracker();
    }

    public void resetState() {
        timingTracker.resetState();
    }

    public void process(boolean isPressed, long currentTime) {
        timingTracker.updatePressState(isPressed);
        networkManager.resetUpdateFlag();

        KeyData.KeyState oldState = keyData.state;
        int oldRapidCount = keyData.rapidClickCount;
        float oldPower = keyData.power;

        if (keyData.checkCooldown(currentTime)) {
            showDebugMessage(String.format("Cooldown: %d ms remaining",
                    keyData.getRemainingCooldown(currentTime)));
            return;
        }

        handleRapidClickCooldown(currentTime);

        KeyState newState = handleStateTransition(currentTime);
        if (newState != currentState) {
            transitionToState(newState, currentTime);
        }

        if (!networkManager.isUpdatedThisTick()) {
            networkManager.checkAndSendUpdates(currentTime);
        }
    }

    private void handleRapidClickCooldown(long currentTime) {
        boolean wasInCooldown = keyData.inRapidClickCooldown;
        boolean nowInCooldown = timingTracker.checkRapidClickCooldown(currentTime);
        
        if (wasInCooldown && !nowInCooldown) {
            showDebugMessage("Rapid click cooldown ended");
        }
        
        keyData.inRapidClickCooldown = nowInCooldown;
    }

    private KeyState handleStateTransition(long currentTime) {
        if (timingTracker.isJustPressed()) {
            showDebugMessage("Key pressed - current state: " + currentState.getClass().getSimpleName());
            timingTracker.setPressedTriggered(false);
            return currentState.handlePress(this, currentTime);
        } 
        else if (timingTracker.isJustReleased()) {
            showDebugMessage("Key released - current state: " + currentState.getClass().getSimpleName() + 
                    ", pressTriggered: " + timingTracker.isPressedTriggered() + 
                    ", pressStartTime: " + timingTracker.getPressStartTime());

            if (handleQuickPressRelease(currentTime)) {
                return currentState;
            }
            
            return currentState.handleRelease(this, currentTime);
        } 
        else {
            return currentState.handleTick(this, currentTime);
        }
    }

    private boolean handleQuickPressRelease(long currentTime) {
        if (!(currentState instanceof IdleState) || 
            timingTracker.getPressStartTime() <= 0 || 
            timingTracker.isPressedTriggered() || 
            config.disableNormalClick) {
            return false;
        }
        
        showDebugMessage("Quick press-release detected - moving to PressedState first");
        timingTracker.setPressedTriggered(true);
        currentState = new PressedState();
        keyData.state = currentState.getKeyDataState();
        
        networkManager.sendNetworkUpdate();
        showDebugMessage("PRESSED state sent to server (force)");
        return true;
    }

    private void transitionToState(KeyState newState, long currentTime) {
        showDebugMessage("State changed: " + currentState.getClass().getSimpleName() + 
                " -> " + newState.getClass().getSimpleName());
        
        KeyState previousState = currentState;
        currentState = newState;
        keyData.state = currentState.getKeyDataState();
        if (isImportantStateChange() || isCriticalStateChange()) {
            networkManager.sendNetworkUpdate();
            showDebugMessage("Important state change - sending update: " + keyData.state);
        }

        if (currentState instanceof IdleState && !(previousState instanceof IdleState)) {
            resetState();
        }
    }

    private boolean isImportantStateChange() {
        return currentState instanceof PressedState || 
               currentState instanceof ReleasedState ||
               currentState instanceof HeldState ||
               currentState instanceof ChargingState ||
               currentState instanceof FinishedState;
    }

    private boolean isCriticalStateChange() {
        return keyData.state == KeyData.KeyState.HELD_RELEASED && 
               networkManager.getLastSentState() != KeyData.KeyState.HELD_RELEASED;
    }

    public KeyConfig getConfig() {
        return config;
    }

    public KeyData getKeyData() {
        return keyData;
    }

    public NetworkSyncManager getNetworkManager() {
        return networkManager;
    }

    public KeyTimingTracker getTimingTracker() {
        return timingTracker;
    }

    public void forceState(KeyData.KeyState state) {
        keyData.state = state;
        networkManager.forceLastSentState(state);

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
                timingTracker.setInTimeoutState(true);
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
                timingTracker.setPressedTriggered(false);
                break;
        }
    }

    public void startRapidClickCooldown(long duration, long currentTime) {
        if (duration <= 0) return;
        timingTracker.startRapidClickCooldown(duration, currentTime);
        keyData.inRapidClickCooldown = true;
    }

    public void triggerRapidClickEvent() {
        keyData.state = KeyData.KeyState.RAPID_CLICK;
        networkManager.sendNetworkUpdate();
        showDebugMessage("RAPID_CLICK state sent to server");
    }

    public void completeRapidClickSequence(long currentTime) {
        if (networkManager.isUpdatedThisTick()) {
            showDebugMessage("Skipping duplicate rapid click complete call");
            return;
        }

        keyData.state = KeyData.KeyState.RAPID_FINISH;
        networkManager.sendNetworkUpdate();
        networkManager.markAsUpdated();
        
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
        keyData.rapidClickCount = 0;
        keyData.clearTimestamps();

        if (reachedCount > 1) {
            if (config.rapidClickResetCooldown > 0) {
                startRapidClickCooldown(config.rapidClickResetCooldown, currentTime);
                showDebugMessage("Starting rapid click reset cooldown: " +
                        config.rapidClickResetCooldown + "ms");
            }
            if (config.showDebugMessage) {
                showDebugMessage("Rapid Click Reset (reached " + reachedCount + " clicks)");
            }
            keyData.state = KeyData.KeyState.IDLE;
            
            if (!networkManager.isUpdatedThisTick()) {
                networkManager.sendNetworkUpdate();
                networkManager.markAsUpdated();
            }
        }
    }

    public void sendNetworkUpdate() {
        networkManager.sendNetworkUpdate();
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