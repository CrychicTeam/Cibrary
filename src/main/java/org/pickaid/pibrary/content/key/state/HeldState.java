package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class HeldState implements KeyState {
    private long lastHeldCheckTime = 0;
    
    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        if (context.getLastSentState() != KeyData.KeyState.HELD) {
            context.showDebugMessage("Re-confirming HELD state in handlePress");
            context.getKeyData().state = KeyData.KeyState.HELD;
            context.sendNetworkUpdate();
            context.forceLastSentState(KeyData.KeyState.HELD);
        }
        return this;
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        context.showDebugMessage(String.format("Manual release from HELD state - Power: %.1f",
                context.getKeyData().power));

        if (context.getLastSentState() != KeyData.KeyState.HELD_RELEASED) {
            context.getKeyData().state = KeyData.KeyState.HELD_RELEASED;
            context.sendNetworkUpdate();
            context.forceLastSentState(KeyData.KeyState.HELD_RELEASED);
        }
        
        if (context.getConfig().releaseCooldown > 0) {
            context.getKeyData().startCooldown(context.getConfig().releaseCooldown);
            context.showDebugMessage("Started release cooldown: " +
                    context.getConfig().releaseCooldown + "ms");
        }

        if (context.getConfig().physicalReleaseDelay > 0) {
            context.setChargedReleaseTime(currentTime);
            context.showDebugMessage("Starting release delay: " +
                    context.getConfig().physicalReleaseDelay + "ms");
            return new AwaitingReleaseState();
        }

        context.setHeldStartTime(0);
        context.setPressStartTime(0);
        context.setPressedTriggered(false);

        HeldReleasedState nextState = new HeldReleasedState();
        context.showDebugMessage("Transitioning to HeldReleasedState");
        return nextState;
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        if (lastHeldCheckTime == 0 || currentTime - lastHeldCheckTime > 500) {
            if (context.getLastSentState() != KeyData.KeyState.HELD) {
                context.showDebugMessage("Re-confirming HELD state in tick");
                context.getKeyData().state = KeyData.KeyState.HELD;
                context.sendNetworkUpdate();
                context.forceLastSentState(KeyData.KeyState.HELD);
            }
            lastHeldCheckTime = currentTime;
        }

        if (context.getConfig().autoReleaseOnMax && !context.isPressed()) {
            context.showDebugMessage("Key auto-released from HELD state");
            return handleRelease(context, currentTime);
        }

        long timeoutDuration = context.getConfig().timeoutTime > 0 ?
                context.getConfig().timeoutTime : KeyStateMachine.DEFAULT_STATE_TIMEOUT;

        if (context.getHeldStartTime() > 0) {
            long heldDuration = currentTime - context.getHeldStartTime();
            if (heldDuration > timeoutDuration) {
                context.showDebugMessage(String.format("Held Timeout after %d ms", heldDuration));
                context.setInTimeoutState(true);
                return new TimeoutState();
            }
        }

        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.HELD;
    }
}