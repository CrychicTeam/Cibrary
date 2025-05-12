package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class CooldownState implements KeyState {
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
        return this;
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        if (isAutoReleaseCooldown) {
            context.showDebugMessage("Physical release during auto-release cooldown");
            isAutoReleaseCooldown = false;
        }

        return this;
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        if (!context.getKeyData().checkCooldown(currentTime)) {
            context.showDebugMessage("Cooldown completed");
            if (isAutoReleaseCooldown && context.getConfig().physicalReleaseDelay > 0) {
                context.setChargedReleaseTime(currentTime);
                context.showDebugMessage("Starting release delay: " +
                        context.getConfig().physicalReleaseDelay + "ms");
                return new AwaitingReleaseState();
            }

            return new IdleState();
        }
        if (currentTime - lastCooldownMessageTime > 200) {
            long remaining = context.getKeyData().getRemainingCooldown(currentTime);

            if (isAutoReleaseCooldown) {
                context.showDebugMessage(String.format("Auto-release cooldown: %d ms remaining", remaining));
            } else {
                context.showDebugMessage(String.format("Cooldown: %d ms remaining", remaining));
            }

            lastCooldownMessageTime = currentTime;
        }

        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.COOLDOWN;
    }
}