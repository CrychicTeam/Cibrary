package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.AbstractKeyState;
import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class HeldState extends AbstractKeyState {

    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        return super.handlePress(context, currentTime);
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        long heldDuration = currentTime - context.getTimingTracker().getHeldStartTime();
        sendDebugMessage(context, "Held state released after " + heldDuration + "ms");
        context.getKeyData().state = KeyData.KeyState.HELD_RELEASED;
        context.getNetworkManager().sendNetworkUpdate();

        if (context.getConfig().releaseCooldown > 0) {
            startCooldown(context, context.getConfig().releaseCooldown);
            sendDebugMessage(context, "Started release cooldown: " + context.getConfig().releaseCooldown + "ms");
            return new CooldownState();
        }

        if (context.getConfig().physicalReleaseDelay > 0) {
            context.getTimingTracker().setChargedReleaseTime(currentTime);
            sendDebugMessage(context, "Starting physical release delay: " + 
                   context.getConfig().physicalReleaseDelay + "ms");
            return new AwaitingReleaseState();
        }

        return new HeldReleasedState();
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        if (isTimeout(context, currentTime, context.getTimingTracker().getHeldStartTime())) {
            sendDebugMessage(context, "Hold timeout reached");
            context.getTimingTracker().setInTimeoutState(true);
            return new TimeoutState();
        }

        long timeSinceHeldStart = currentTime - context.getTimingTracker().getHeldStartTime();
        if (timeSinceHeldStart % KeyStateMachine.CHARGING_UPDATE_INTERVAL == 0) {
            context.getNetworkManager().sendNetworkUpdate();
        }
        
        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.HELD;
    }
}