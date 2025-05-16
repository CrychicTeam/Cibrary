package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.AbstractKeyState;
import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class RapidFinishState extends AbstractKeyState {
    private boolean hasProcessed = false;

    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        return this;
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        return this;
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        if (!hasProcessed) hasProcessed = true;
        if (!context.getKeyData().inCooldown) return new IdleState();
        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.RAPID_FINISH;
    }
}