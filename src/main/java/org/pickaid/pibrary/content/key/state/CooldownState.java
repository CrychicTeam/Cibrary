package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.AbstractKeyState;
import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class CooldownState extends AbstractKeyState {
    private long lastCooldownMessageTime = 0;
    private boolean isAutoReleaseCooldown = false;

    public CooldownState() {
        this(false);
    }

    public CooldownState(boolean isAutoReleaseCooldown) {
        this.isAutoReleaseCooldown = isAutoReleaseCooldown;
    }

    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        return super.handlePress(context, currentTime);
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        if (isAutoReleaseCooldown) {
            sendDebugMessage(context, "Physical release during auto-release cooldown");
            isAutoReleaseCooldown = false;
        }

        return this;
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        if (!context.getKeyData().checkCooldown(currentTime)) {
            sendDebugMessage(context, "Cooldown completed");
            if (isAutoReleaseCooldown && context.getConfig().physicalReleaseDelay > 0) {
                context.getTimingTracker().setChargedReleaseTime(currentTime);
                sendDebugMessage(context, "Starting release delay: " +
                        context.getConfig().physicalReleaseDelay + "ms");
                return new AwaitingReleaseState();
            }
            return new IdleState();
        }

        if (currentTime - lastCooldownMessageTime > 200) {
            lastCooldownMessageTime = currentTime;
        }

        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.COOLDOWN;
    }
}