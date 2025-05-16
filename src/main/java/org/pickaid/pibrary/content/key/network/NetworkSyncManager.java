package org.pickaid.pibrary.content.key.network;

import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;
import org.pickaid.pibrary.network.PibraryNetworkHandler;
import org.pickaid.pibrary.network.key.KeyStatePacket;

public class NetworkSyncManager {
    private final KeyStateMachine stateMachine;

    private KeyData.KeyState lastSentState = KeyData.KeyState.IDLE;
    private float lastSentPower = 0f;
    private int lastSentRapidCount = 0;
    private long lastUpdateTime = 0;

    private static final float POWER_UPDATE_THRESHOLD = KeyStateMachine.POWER_UPDATE_THRESHOLD;
    private static final long CHARGING_UPDATE_INTERVAL = KeyStateMachine.CHARGING_UPDATE_INTERVAL;

    private boolean updatedThisTick = false;
    private boolean hasRapidFinishBeenSent = false;

    public NetworkSyncManager(KeyStateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    public void resetUpdateFlag() {
        updatedThisTick = false;
    }

    public void markAsUpdated() {
        updatedThisTick = true;
    }

    public boolean isUpdatedThisTick() {
        return updatedThisTick;
    }

    public KeyData.KeyState getLastSentState() {
        return lastSentState;
    }

    public void forceLastSentState(KeyData.KeyState state) {
        lastSentState = state;
        if (state == KeyData.KeyState.IDLE) {
            hasRapidFinishBeenSent = false;
        }
    }

    public float getLastSentPower() {
        return lastSentPower;
    }

    public void checkAndSendUpdates(long currentTime) {
        if (updatedThisTick) {
            return;
        }

        KeyData keyData = stateMachine.getKeyData();
        boolean shouldSendUpdate = false;
        long timeSinceLastUpdate = currentTime - lastUpdateTime;

        if (keyData.state == KeyData.KeyState.RAPID_FINISH) {
            if (hasRapidFinishBeenSent) {
                stateMachine.showDebugMessage("RAPID_FINISH already sent, skipping duplicate");
                return;
            }
            hasRapidFinishBeenSent = true;
            shouldSendUpdate = true;
        }
        else if (hasRapidFinishBeenSent && keyData.state != KeyData.KeyState.RAPID_FINISH) {
            hasRapidFinishBeenSent = false;
        }
        if (lastSentState != keyData.state) {
            shouldSendUpdate = true;
            if (isCriticalStateChange(keyData.state)) {
                stateMachine.showDebugMessage("Critical state change detected: " + keyData.state);
                sendNetworkUpdate();
                return;
            }
        }
        else if (lastSentRapidCount != keyData.rapidClickCount) {
            shouldSendUpdate = true;
            lastSentRapidCount = keyData.rapidClickCount;
            stateMachine.showDebugMessage("RapidClick count changed: " + keyData.rapidClickCount);
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
        else if (needsSpecialStateUpdate(keyData.state)) {
            shouldSendUpdate = true;
            stateMachine.showDebugMessage(keyData.state + " state needs to be sent");
        }

        if (shouldSendUpdate) {
            sendNetworkUpdate();
        }
    }

    private boolean isCriticalStateChange(KeyData.KeyState state) {
        return (state == KeyData.KeyState.PRESSED && lastSentState != KeyData.KeyState.PRESSED) ||
                (state == KeyData.KeyState.RELEASED && lastSentState != KeyData.KeyState.RELEASED) ||
                (state == KeyData.KeyState.FINISHED && lastSentState != KeyData.KeyState.FINISHED) ||
                (state == KeyData.KeyState.RAPID_CLICK && lastSentState != KeyData.KeyState.RAPID_CLICK) ||
                (state == KeyData.KeyState.RAPID_FINISH && lastSentState != KeyData.KeyState.RAPID_FINISH) ||
                (state == KeyData.KeyState.HELD_RELEASED && lastSentState != KeyData.KeyState.HELD_RELEASED);
    }

    private boolean needsSpecialStateUpdate(KeyData.KeyState state) {
        return (state == KeyData.KeyState.PRESSED && lastSentState != KeyData.KeyState.PRESSED) ||
                (state == KeyData.KeyState.RAPID_CLICK && lastSentState != KeyData.KeyState.RAPID_CLICK) ||
                (state == KeyData.KeyState.FINISHED && lastSentState != KeyData.KeyState.FINISHED) ||
                (state == KeyData.KeyState.RAPID_FINISH && lastSentState != KeyData.KeyState.RAPID_FINISH) ||
                (state == KeyData.KeyState.HELD_RELEASED && lastSentState != KeyData.KeyState.HELD_RELEASED);
    }

    public void sendNetworkUpdate() {
        KeyData keyData = stateMachine.getKeyData();
        stateMachine.showDebugMessage("Sending network update: " + keyData.state);
        if (keyData.state == KeyData.KeyState.RAPID_FINISH && lastSentState == KeyData.KeyState.RAPID_FINISH) {
            stateMachine.showDebugMessage("Prevented duplicate RAPID_FINISH network update");
            updatedThisTick = true;
            return;
        }

        PibraryNetworkHandler.CHANNEL.sendToServer(new KeyStatePacket(keyData));

        lastSentState = keyData.state;
        lastSentPower = keyData.power;
        lastSentRapidCount = keyData.rapidClickCount;
        lastUpdateTime = System.currentTimeMillis();
        updatedThisTick = true;
    }
}