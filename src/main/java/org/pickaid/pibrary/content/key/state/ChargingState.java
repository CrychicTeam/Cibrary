package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.AbstractKeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;
import org.pickaid.pibrary.api.key.KeyState;

public class ChargingState extends AbstractKeyState {
    private static final float MIN_RELEASE_POWER = KeyStateMachine.LEAST_RELEASE_TIME;
    
    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        return updatePowerAndCheckTransition(context, currentTime);
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        boolean isFullyCharged = context.getKeyData().power >= context.getConfig().maxPower;
        boolean hasEnoughPower = context.getKeyData().power >= MIN_RELEASE_POWER;
        boolean isAutoRelease = isFullyCharged && context.getConfig().autoReleaseOnMax;
        long holdDuration = context.getTimingTracker().getHoldDuration(currentTime);

        sendDebugMessage(context, String.format(
                "ChargingState handling release - Power: %.2f, FullyCharged: %b, EnoughPower: %b, HoldDuration: %d ms", 
                context.getKeyData().power, isFullyCharged, hasEnoughPower, holdDuration));

        if (isFullyCharged && !context.getConfig().autoReleaseOnMax) {
            return handleFullyChargedManualRelease(context);
        }

        if (isAutoRelease || hasEnoughPower) {
            return handleSufficientPowerRelease(context, currentTime);
        }

        if (holdDuration >= context.getConfig().pressTimeTolerance) {
            return handleInsufficientPowerRelease(context);
        }
        sendDebugMessage(context, "Ignored too short press (< " + context.getConfig().pressTimeTolerance + "ms)");
        return new IdleState();
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        return updatePowerAndCheckTransition(context, currentTime);
    }

    private KeyState handleFullyChargedManualRelease(KeyStateMachine context) {
        sendDebugMessage(context, "Fully charged key released manually - going to HELD_RELEASED");

        context.getKeyData().state = KeyData.KeyState.HELD_RELEASED;
        context.getNetworkManager().sendNetworkUpdate();
        context.getNetworkManager().forceLastSentState(KeyData.KeyState.HELD_RELEASED);
        if (context.getConfig().releaseCooldown > 0) {
            startCooldown(context, context.getConfig().releaseCooldown);
        }
        context.getTimingTracker().setHeldStartTime(0);
        context.getTimingTracker().setPressStartTime(0);
        
        return new HeldReleasedState();
    }

    private KeyState handleSufficientPowerRelease(KeyStateMachine context, long currentTime) {
        sendDebugMessage(context, String.format("Released with sufficient power - Power: %.1f",
                context.getKeyData().power));
        context.getKeyData().state = KeyData.KeyState.RELEASED;
        context.getNetworkManager().sendNetworkUpdate();
        context.getNetworkManager().forceLastSentState(KeyData.KeyState.RELEASED);
        if (context.getConfig().releaseCooldown > 0) {
            startCooldown(context, context.getConfig().releaseCooldown);
            return new CooldownState();
        }
        if (context.getConfig().physicalReleaseDelay > 0) {
            context.getTimingTracker().setChargedReleaseTime(currentTime);
            sendDebugMessage(context, "Starting release delay: " +
                    context.getConfig().physicalReleaseDelay + "ms");
            return new AwaitingReleaseState();
        }

        return new ReleasedState();
    }

    private KeyState handleInsufficientPowerRelease(KeyStateMachine context) {
        sendDebugMessage(context, "Key released after sufficient hold duration but insufficient power");
        context.getKeyData().state = KeyData.KeyState.RELEASED;
        context.getNetworkManager().forceLastSentState(KeyData.KeyState.RELEASED);

        return new ReleasedState();
    }

    private KeyState updatePowerAndCheckTransition(KeyStateMachine context, long currentTime) {
        long holdDuration = context.getTimingTracker().getHoldDuration(currentTime);
        float newPower = calculatePower(context, currentTime, context.getTimingTracker().getPressStartTime());
        context.getKeyData().updatePower(newPower, context.getConfig().maxPower);
        if (isTimeout(context, currentTime, context.getTimingTracker().getPressStartTime())) {
            sendDebugMessage(context, "Charge Timeout");
            context.getTimingTracker().setInTimeoutState(true);
            return new TimeoutState();
        }
        if (context.getKeyData().power >= context.getConfig().maxPower) {
            return handleMaxPowerReached(context, currentTime);
        }

        return this;
    }

    private KeyState handleMaxPowerReached(KeyStateMachine context, long currentTime) {
        if (context.getConfig().autoReleaseOnMax) {
            context.getConfig().keyMapping.release();
            if (context.getConfig().releaseCooldown > 0) {
                startCooldown(context, context.getConfig().releaseCooldown);
                sendDebugMessage(context, "Auto Finished - Started cooldown: " +
                        context.getConfig().releaseCooldown + "ms");
                return new CooldownState(true);
            }

            sendDebugMessage(context, "Auto Finished");
            return new FinishedState();
        }
        else {
            context.getTimingTracker().setHeldStartTime(currentTime);
            sendDebugMessage(context, "Max Charge Reached - Entering HELD state");
            context.getKeyData().state = KeyData.KeyState.HELD;
            context.getNetworkManager().sendNetworkUpdate();
            context.getNetworkManager().forceLastSentState(KeyData.KeyState.HELD);
            
            return new HeldState();
        }
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.CHARGING;
    }
}