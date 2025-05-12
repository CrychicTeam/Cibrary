package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class ReleasedState implements KeyState {
    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        if (context.getKeyData().inCooldown) {
            return this;
        }
        return new IdleState().handlePress(context, currentTime);
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        return this;
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        return new IdleState();
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.RELEASED;
    }
}