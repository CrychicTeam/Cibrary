package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class AwaitingReleaseState implements KeyState {
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
        if (context.getChargedReleaseTime() <= 0) {
            return new IdleState();
        }

        long elapsedSinceRelease = currentTime - context.getChargedReleaseTime();
        if (elapsedSinceRelease >= context.getConfig().physicalReleaseDelay) {
            context.showDebugMessage("Release delay completed");
            context.setChargedReleaseTime(0);
            if (context.getConfig().releaseCooldown > 0 && !context.getKeyData().inCooldown) {
                context.getKeyData().startCooldown(context.getConfig().releaseCooldown);
                context.showDebugMessage("Started post-release cooldown: " +
                        context.getConfig().releaseCooldown + "ms");
                return new CooldownState();
            }

            return new IdleState();
        } else {
            long remaining = context.getConfig().physicalReleaseDelay - elapsedSinceRelease;
            context.showDebugMessage(String.format("Release delay: %d ms remaining", remaining));
        }

        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.AWAITING_RELEASE;
    }
}