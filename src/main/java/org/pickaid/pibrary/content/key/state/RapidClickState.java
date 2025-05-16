package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.AbstractKeyState;
import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class RapidClickState extends AbstractKeyState {
    private boolean isFinishing = false;

    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        if (isFinishing) {
            return this;
        }

        context.getKeyData().recordPressTimestamp(currentTime);

        if (context.getKeyData().inRapidClickCooldown) {
            sendDebugMessage(context, "Click during cooldown ignored in RAPID_CLICK state");
            return this;
        }

        context.getKeyData().rapidClickCount++;
        int maxCount = context.getConfig().maxRapidClickCount;

        if (context.getKeyData().rapidClickCount >= maxCount) {
            sendDebugMessage(context, "Max Rapid Clicks Reached! (" + maxCount + ")");
            isFinishing = true;
            context.completeRapidClickSequence(currentTime);
            return new RapidFinishState();
        } else {
            sendDebugMessage(context, "Rapid Click #" + context.getKeyData().rapidClickCount);
            context.triggerRapidClickEvent();
            return this;
        }
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        return this;
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        if (isFinishing) {
            return new RapidFinishState();
        }
        if (context.getKeyData().pressTimestamps[0] > 0) {
            long timeSinceLastClick = currentTime - context.getKeyData().pressTimestamps[0];

            if (timeSinceLastClick > context.getConfig().rapidClickTimeWindow) {
                sendDebugMessage(context, String.format("Rapid Click Reset (exceeded time window of %d ms)",
                        context.getConfig().rapidClickTimeWindow));
                context.resetRapidClick(currentTime);
                return new IdleState();
            }
        }
        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.RAPID_CLICK;
    }
}