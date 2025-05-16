package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.AbstractKeyState;
import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class FinishedState extends AbstractKeyState {
    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        return super.handlePress(context, currentTime);
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        return this;
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        if (!context.getKeyData().inCooldown) {
            return new IdleState();
        }
        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.FINISHED;
    }
}