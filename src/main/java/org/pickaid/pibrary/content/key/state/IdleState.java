package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.AbstractKeyState;
import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class IdleState extends AbstractKeyState {
    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        context.getTimingTracker().setPressStartTime(currentTime);
        context.getTimingTracker().setPressedTriggered(false);

        sendDebugMessage(context, "Key Press detected in IDLE state");

        if (context.getConfig().enableRapidClick) {
            return handleRapidClickPress(context, currentTime);
        }

        if (context.getConfig().disableNormalClick && context.getConfig().enableCharging) {
            sendDebugMessage(context, "Entering charging state directly (normal click disabled)");
            return startCharging(context, currentTime);
        }

        if (!context.getConfig().enableCharging) {
            sendDebugMessage(context, "Regular key press - entering PRESSED state");
            context.getTimingTracker().setPressedTriggered(true);
            return new PressedState();
        }

        sendDebugMessage(context, "Ready for charging if held");
        return this;
    }

    private KeyState handleRapidClickPress(KeyStateMachine context, long currentTime) {
        if (context.getKeyData().inRapidClickCooldown) {
            sendDebugMessage(context, "Click during cooldown ignored");
            return this;
        }

        context.getKeyData().recordPressTimestamp(currentTime);
        boolean hasRecentClick = context.getKeyData().pressTimestamps[1] > 0;

        if (hasRecentClick) {
            return handleRecentClick(context, currentTime);
        }

        context.getKeyData().rapidClickCount = 1;
        sendDebugMessage(context, "First Click");

        return handleNormalOrChargingClick(context);
    }

    private KeyState handleRecentClick(KeyStateMachine context, long currentTime) {
        long timeSincePreviousClick = currentTime - context.getKeyData().pressTimestamps[1];

        if (timeSincePreviousClick < context.getConfig().rapidClickTimeWindow) {
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

        context.getKeyData().rapidClickCount = 1;
        sendDebugMessage(context, "New Click Sequence");

        return handleNormalOrChargingClick(context);
    }

    private KeyState handleNormalOrChargingClick(KeyStateMachine context) {
        if (!context.getConfig().disableNormalClick) {
            if (context.getConfig().enableCharging) {
                sendDebugMessage(context, "Ready for charging if held");
            }
            return this;
        }

        if (context.getConfig().enableCharging) {
            sendDebugMessage(context, "Click with charging enabled - waiting for potential charging");
            return this;
        }

        sendDebugMessage(context, "Click with normal click disabled - entering RAPID_CLICK state");
        context.triggerRapidClickEvent();
        return new RapidClickState();
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        if (context.getTimingTracker().getPressStartTime() <= 0) {
            return this;
        }

        if (context.getConfig().disableNormalClick) {
            sendDebugMessage(context, "Normal click disabled by config");
            context.getTimingTracker().setPressStartTime(0);
            return this;
        }

        sendDebugMessage(context, "Key Press and Release detected - treating as normal click");
        context.getTimingTracker().setPressedTriggered(true);
        context.getTimingTracker().setPressStartTime(0);

        if (context.getConfig().pressCooldown > 0) {
            startCooldown(context, context.getConfig().pressCooldown);
            sendDebugMessage(context, "Started press cooldown: " + context.getConfig().pressCooldown + "ms");
            return new CooldownState();
        }

        return new PressedState();
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        if (!context.getTimingTracker().isPressed() || context.getTimingTracker().getPressStartTime() <= 0) {
            return this;
        }

        long holdDuration = currentTime - context.getTimingTracker().getPressStartTime();

        if (shouldStartCharging(context, holdDuration)) {
            sendDebugMessage(context, "Start Charging (duration: " + holdDuration + "ms)");
            return startCharging(context, currentTime);
        }

        if (shouldTriggerNormalPress(context, holdDuration)) {
            context.getTimingTracker().setPressedTriggered(true);
            sendDebugMessage(context, "Key Pressed (normal hold)");
            return new PressedState();
        }

        if (shouldDetectMissedClick(context)) {
            sendDebugMessage(context, "Detecting missed click in tick - processing now");
            context.getTimingTracker().setPressedTriggered(true);
            context.getTimingTracker().setPressStartTime(0);
            return new PressedState();
        }

        return this;
    }

    private boolean shouldStartCharging(KeyStateMachine context, long holdDuration) {
        return context.getConfig().enableCharging &&
                holdDuration > context.getConfig().chargingTimeTolerance;
    }

    private boolean shouldTriggerNormalPress(KeyStateMachine context, long holdDuration) {
        return !context.getConfig().enableCharging &&
                holdDuration >= context.getConfig().pressTimeTolerance &&
                !context.getTimingTracker().isPressedTriggered() &&
                !context.getConfig().disableNormalClick;
    }

    private boolean shouldDetectMissedClick(KeyStateMachine context) {
        return !context.getTimingTracker().isPressed() &&
                !context.getTimingTracker().isPressedTriggered() &&
                !context.getConfig().disableNormalClick;
    }

    private KeyState startCharging(KeyStateMachine context, long currentTime) {
        sendDebugMessage(context, "Starting charging...");
        context.getKeyData().updatePower(0.0f, context.getConfig().maxPower);
        context.getKeyData().state = KeyData.KeyState.CHARGING;
        return new ChargingState();
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.IDLE;
    }
}