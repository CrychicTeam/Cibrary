package org.pickaid.pibrary.api.key;

import org.pickaid.pibrary.content.key.KeyStateMachine;
import org.pickaid.pibrary.content.key.state.IdleState;

public abstract class AbstractKeyState implements KeyState {

    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        return this;
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        return new IdleState();
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        return this;
    }

    protected void sendDebugMessage(KeyStateMachine context, String message) {
        context.showDebugMessage(message);
    }

    protected boolean isTimeout(KeyStateMachine context, long currentTime, long startTime) {
        long timeoutDuration = context.getConfig().timeoutTime > 0 ?
                context.getConfig().timeoutTime : KeyStateMachine.DEFAULT_STATE_TIMEOUT;
        return (currentTime - startTime) > timeoutDuration;
    }

    protected float calculatePower(KeyStateMachine context, long currentTime, long startTime) {
        long holdDuration = currentTime - startTime;
        return Math.min((float) holdDuration / 1000.0f, context.getConfig().maxPower);
    }

    protected void startCooldown(KeyStateMachine context, long duration) {
        if (duration <= 0) return;
        context.getKeyData().startCooldown(duration);
        sendDebugMessage(context, "Started cooldown: " + duration + "ms");
    }
} 