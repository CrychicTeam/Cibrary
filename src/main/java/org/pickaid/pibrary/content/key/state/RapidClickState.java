package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class RapidClickState implements KeyState {
    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        context.showDebugMessage("New click in RAPID_CLICK state");
        
        context.getKeyData().recordPressTimestamp(currentTime);
        
        if (!context.getKeyData().inRapidClickCooldown) {
            context.getKeyData().rapidClickCount++;
            int maxCount = context.getConfig().maxRapidClickCount > 0 ?
                    context.getConfig().maxRapidClickCount : 5;
                    
            if (context.getKeyData().rapidClickCount >= maxCount) {
                context.showDebugMessage("Max Rapid Clicks Reached! (" + maxCount + ")");
                context.completeRapidClickSequence(currentTime);
                return new RapidFinishState();
            } else {
                context.showDebugMessage("Rapid Click #" + context.getKeyData().rapidClickCount);
                context.triggerRapidClickEvent();
                return this;
            }
        } else {
            context.showDebugMessage("Click during cooldown ignored in RAPID_CLICK state");
            return this;
        }
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        return this;
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        if (context.getKeyData().pressTimestamps[0] > 0) {
            long timeSinceLastClick = currentTime - context.getKeyData().pressTimestamps[0];
            if (timeSinceLastClick > KeyStateMachine.RAPID_CLICK_INTERVAL) {
                context.resetRapidClick(currentTime);
                context.showDebugMessage("Rapid Click Reset (timeout)");
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