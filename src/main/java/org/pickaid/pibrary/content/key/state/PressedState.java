package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class PressedState implements KeyState {
    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        context.showDebugMessage("Re-pressing in PRESSED state - reinforcing network message");
        context.sendNetworkUpdate();
        return this;
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        context.showDebugMessage("PressedState handling release");
        if (context.getLastSentState() != KeyData.KeyState.PRESSED) {
            context.showDebugMessage("Ensuring PRESSED state is sent before release");
            context.sendNetworkUpdate();
        }
        
        if (context.getConfig().pressCooldown > 0) {
            context.getKeyData().startCooldown(context.getConfig().pressCooldown);
            context.showDebugMessage("Started press cooldown: " +
                    context.getConfig().pressCooldown + "ms");
            return new CooldownState();
        }
        context.setPressedTriggered(false);
        context.setPressStartTime(0);
        return new IdleState();
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.PRESSED;
    }
}