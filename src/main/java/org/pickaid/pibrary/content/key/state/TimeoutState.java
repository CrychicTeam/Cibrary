package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class TimeoutState implements KeyState {
    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        return this;
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        context.showDebugMessage("Key released from timeout state");
        context.setPressStartTime(0);
        context.setInTimeoutState(false);
        context.setHeldStartTime(0);
        context.setPressedTriggered(false);
        return new IdleState();
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.TIMEOUT;
    }
}