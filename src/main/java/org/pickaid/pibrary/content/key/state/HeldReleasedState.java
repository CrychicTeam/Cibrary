package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.AbstractKeyState;
import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class HeldReleasedState extends AbstractKeyState {
    private boolean hasTriedToSendUpdate = false;
    
    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        if (context.getKeyData().inCooldown) {
            sendDebugMessage(context, "Press ignored during HELD_RELEASED cooldown");
            return this;
        }

        sendDebugMessage(context, "New press while in HELD_RELEASED state - transitioning to IdleState");
        context.getTimingTracker().setPressedTriggered(false);
        context.getTimingTracker().setPressStartTime(0);

        context.getKeyData().state = KeyData.KeyState.IDLE;
        context.getNetworkManager().sendNetworkUpdate();
        context.getNetworkManager().forceLastSentState(KeyData.KeyState.IDLE);
        
        return new IdleState().handlePress(context, currentTime);
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        if (context.getNetworkManager().getLastSentState() != KeyData.KeyState.HELD_RELEASED && !hasTriedToSendUpdate) {
            sendDebugMessage(context, ">>>>> Re-sending HELD_RELEASED state - CRITICAL UPDATE <<<<<");
            context.getKeyData().state = KeyData.KeyState.HELD_RELEASED;
            context.getNetworkManager().sendNetworkUpdate();
            context.getNetworkManager().forceLastSentState(KeyData.KeyState.HELD_RELEASED);
            hasTriedToSendUpdate = true;
        } else if (hasTriedToSendUpdate) {
            sendDebugMessage(context, "Already tried to send HELD_RELEASED, won't try again");
        } else {
            sendDebugMessage(context, "HELD_RELEASED already sent to server");
        }
        return this;
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        if (context.getNetworkManager().getLastSentState() != KeyData.KeyState.HELD_RELEASED && !hasTriedToSendUpdate) {
            context.getKeyData().state = KeyData.KeyState.HELD_RELEASED;
            context.getNetworkManager().sendNetworkUpdate();
            context.getNetworkManager().forceLastSentState(KeyData.KeyState.HELD_RELEASED);
            hasTriedToSendUpdate = true;
        }

        if (context.getKeyData().inCooldown) {
                    context.getKeyData().getRemainingCooldown(currentTime);
        }

        if (!context.getKeyData().inCooldown) {
            context.getKeyData().state = KeyData.KeyState.IDLE;
            context.getNetworkManager().sendNetworkUpdate();
            context.getNetworkManager().forceLastSentState(KeyData.KeyState.IDLE);
            
            return new IdleState();
        }
        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.HELD_RELEASED;
    }

    public KeyState reset(KeyStateMachine context) {
        sendDebugMessage(context, "Manually resetting HELD_RELEASED state");
        context.getKeyData().inCooldown = false;
        context.getKeyData().state = KeyData.KeyState.IDLE;
        context.getNetworkManager().sendNetworkUpdate();
        context.getNetworkManager().forceLastSentState(KeyData.KeyState.IDLE);
        return new IdleState();
    }
}