package org.pickaid.pibrary.api.key;

import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public interface KeyState {
    KeyState handlePress(KeyStateMachine context, long currentTime);
    KeyState handleRelease(KeyStateMachine context, long currentTime);
    KeyState handleTick(KeyStateMachine context, long currentTime);
    KeyData.KeyState getKeyDataState();
}