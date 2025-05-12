package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class ChargingState implements KeyState {
    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        return updatePowerAndCheckTransition(context, currentTime);
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        boolean isFullyCharged = context.getKeyData().power >= context.getConfig().maxPower;
        boolean hasEnoughPower = context.getKeyData().power >= KeyStateMachine.LEAST_RELEASE_TIME;
        boolean isAutoRelease = isFullyCharged && context.getConfig().autoReleaseOnMax;
        long holdDuration = currentTime - context.getPressStartTime();

        context.showDebugMessage(String.format("ChargingState handling release - Power: %.2f, FullyCharged: %b, EnoughPower: %b, HoldDuration: %d ms", 
                context.getKeyData().power, isFullyCharged, hasEnoughPower, holdDuration));

        if (isFullyCharged && !context.getConfig().autoReleaseOnMax) {
            context.showDebugMessage("Fully charged key released manually - going to HELD_RELEASED");
            
            context.getKeyData().state = KeyData.KeyState.HELD_RELEASED;
            context.sendNetworkUpdate();
            context.forceLastSentState(KeyData.KeyState.HELD_RELEASED);
            
            if (context.getConfig().releaseCooldown > 0) {
                context.getKeyData().startCooldown(context.getConfig().releaseCooldown);
                context.showDebugMessage("Started release cooldown: " +
                        context.getConfig().releaseCooldown + "ms");
            }
            
            context.setHeldStartTime(0);
            context.setPressStartTime(0);
            
            return new HeldReleasedState();
        }

        if (isAutoRelease || hasEnoughPower) {
            context.showDebugMessage(String.format("Released with sufficient power - Power: %.1f",
                    context.getKeyData().power));

            context.getKeyData().state = KeyData.KeyState.RELEASED;
            context.sendNetworkUpdate();
            context.forceLastSentState(KeyData.KeyState.RELEASED);

            if (context.getConfig().releaseCooldown > 0) {
                context.getKeyData().startCooldown(context.getConfig().releaseCooldown);
                context.showDebugMessage("Started release cooldown: " +
                        context.getConfig().releaseCooldown + "ms");
                return new CooldownState();
            }

            if (context.getConfig().physicalReleaseDelay > 0) {
                context.setChargedReleaseTime(currentTime);
                context.showDebugMessage("Starting release delay: " +
                        context.getConfig().physicalReleaseDelay + "ms");
                return new AwaitingReleaseState();
            }

            return new ReleasedState();
        }

        else if (holdDuration >= context.getConfig().pressTimeTolerance) {
            context.showDebugMessage("Key released after sufficient hold duration but insufficient power");

            context.getKeyData().state = KeyData.KeyState.RELEASED;
            context.forceLastSentState(KeyData.KeyState.RELEASED);

            return new ReleasedState();
        }

        else {
            context.showDebugMessage("Ignored too short press (< " + context.getConfig().pressTimeTolerance + "ms)");
            return new IdleState();
        }
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        return updatePowerAndCheckTransition(context, currentTime);
    }

    private KeyState updatePowerAndCheckTransition(KeyStateMachine context, long currentTime) {
        long holdDuration = currentTime - context.getPressStartTime();
        float newPower = Math.min((float) holdDuration / 1000.0f, context.getConfig().maxPower);
        context.getKeyData().updatePower(newPower, context.getConfig().maxPower);
        long timeoutDuration = context.getConfig().timeoutTime > 0 ?
                context.getConfig().timeoutTime : KeyStateMachine.DEFAULT_STATE_TIMEOUT;

        if (holdDuration > timeoutDuration) {
            context.showDebugMessage("Charge Timeout");
            context.setInTimeoutState(true);
            return new TimeoutState();
        }
        
        if (context.getKeyData().power >= context.getConfig().maxPower) {
            if (context.getConfig().autoReleaseOnMax) {
                context.getConfig().keyMapping.release();

                if (context.getConfig().releaseCooldown > 0) {
                    context.getKeyData().startCooldown(context.getConfig().releaseCooldown);
                    context.showDebugMessage("Auto Finished - Started cooldown: " +
                            context.getConfig().releaseCooldown + "ms");
                    return new CooldownState(true);
                }

                context.showDebugMessage("Auto Finished");
                return new FinishedState();
            } else {
                context.setHeldStartTime(currentTime);
                context.showDebugMessage("Max Charge Reached - Entering HELD state");
                
                context.getKeyData().state = KeyData.KeyState.HELD;
                context.sendNetworkUpdate();
                context.forceLastSentState(KeyData.KeyState.HELD);
                
                return new HeldState();
            }
        }

        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.CHARGING;
    }
}