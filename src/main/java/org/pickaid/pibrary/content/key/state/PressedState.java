package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.AbstractKeyState;
import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class PressedState extends AbstractKeyState {
    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        if (!context.getConfig().enableRapidClick) {
            context.getNetworkManager().sendNetworkUpdate();
            return this;
        }
        sendDebugMessage(context, "Processing rapid click in PRESSED state");
        context.getKeyData().recordPressTimestamp(currentTime);
        if (context.getKeyData().pressTimestamps[1] <= 0) {
            context.getKeyData().rapidClickCount = 1;
            sendDebugMessage(context, "First Click in sequence");
            return this;
        }
        long timeSincePreviousClick = currentTime - context.getKeyData().pressTimestamps[1];
        boolean isRapidClick = timeSincePreviousClick < context.getConfig().rapidClickTimeWindow;
        if (!isRapidClick) {
            context.getKeyData().rapidClickCount = 1;
            sendDebugMessage(context, "New Rapid Click Sequence");
            return this;
        }
        context.getKeyData().rapidClickCount++;
        sendDebugMessage(context, "Rapid Click #" + context.getKeyData().rapidClickCount);
        context.triggerRapidClickEvent();
        int maxCount = context.getConfig().maxRapidClickCount > 0 ?
                context.getConfig().maxRapidClickCount : 5;
        if (context.getKeyData().rapidClickCount >= maxCount) {
            sendDebugMessage(context, "Max Rapid Clicks Reached!");
            context.completeRapidClickSequence(currentTime);
            return new RapidFinishState();
        }

        return new RapidClickState();
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        if (context.getNetworkManager().getLastSentState() != KeyData.KeyState.PRESSED) {
            context.getNetworkManager().sendNetworkUpdate();
        }
        if (context.getConfig().pressCooldown > 0) {
            startCooldown(context, context.getConfig().pressCooldown);
            return new CooldownState();
        }
        context.getTimingTracker().setPressedTriggered(false);
        context.getTimingTracker().setPressStartTime(0);
        return new IdleState();
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        return super.handleTick(context, currentTime);
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.PRESSED;
    }
}