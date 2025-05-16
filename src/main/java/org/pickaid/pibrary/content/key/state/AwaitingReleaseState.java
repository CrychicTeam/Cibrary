package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.AbstractKeyState;
import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class AwaitingReleaseState extends AbstractKeyState {
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
        if (context.getTimingTracker().getChargedReleaseTime() <= 0) {
            return new IdleState();
        }

        long elapsedSinceRelease = currentTime - context.getTimingTracker().getChargedReleaseTime();
        if (elapsedSinceRelease >= context.getConfig().physicalReleaseDelay) {
            sendDebugMessage(context, "Release delay completed");
            context.getTimingTracker().setChargedReleaseTime(0);

            if (context.getConfig().releaseCooldown > 0 && !context.getKeyData().inCooldown) {
                startCooldown(context, context.getConfig().releaseCooldown);
                sendDebugMessage(context, "Started post-release cooldown: " +
                        context.getConfig().releaseCooldown + "ms");
                return new CooldownState();
            }

            return new IdleState();
        } else {
            long remaining = context.getConfig().physicalReleaseDelay - elapsedSinceRelease;
            sendDebugMessage(context, String.format("Release delay: %d ms remaining", remaining));
        }

        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.AWAITING_RELEASE;
    }
}