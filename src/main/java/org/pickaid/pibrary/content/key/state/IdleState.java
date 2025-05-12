package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

public class IdleState implements KeyState {
    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        context.setPressStartTime(currentTime);
        context.setPressedTriggered(false);
        
        context.showDebugMessage("Key Press detected in IDLE state");

        if (context.getConfig().enableRapidClick) {
            context.getKeyData().recordPressTimestamp(currentTime);

            if (context.getKeyData().pressTimestamps[1] > 0 &&
                    !context.getKeyData().inRapidClickCooldown) {
                long timeSincePreviousClick = currentTime - context.getKeyData().pressTimestamps[1];

                if (timeSincePreviousClick < KeyStateMachine.RAPID_CLICK_THRESHOLD) {
                    context.getKeyData().rapidClickCount++;
                    int maxCount = context.getConfig().maxRapidClickCount > 0 ?
                            context.getConfig().maxRapidClickCount : 5;

                    if (context.getKeyData().rapidClickCount >= maxCount) {
                        context.showDebugMessage("Max Rapid Clicks Reached! (" + maxCount + ")");
                        context.completeRapidClickSequence(currentTime);
                        return new FinishedState();
                    } else {
                        context.showDebugMessage("Rapid Click #" + context.getKeyData().rapidClickCount);
                        context.triggerRapidClickEvent();
                        return new RapidClickState();
                    }
                } else {
                    context.getKeyData().rapidClickCount = 1;
                    context.showDebugMessage("New Click Sequence");
                    
                    if (context.getConfig().disableNormalClick) {
                        context.showDebugMessage("First click in sequence with normal click disabled");
                        context.triggerRapidClickEvent();
                        return new RapidClickState();
                    }
                }
            } else if (!context.getKeyData().inRapidClickCooldown) {
                context.getKeyData().rapidClickCount = 1;
                context.showDebugMessage("First Click");
                
                if (context.getConfig().disableNormalClick) {
                    context.showDebugMessage("First click with normal click disabled");
                    context.triggerRapidClickEvent();
                    return new RapidClickState();
                }
            } else {
                context.showDebugMessage("Click during cooldown ignored");
            }

            if (context.getConfig().enableCharging) {
                context.showDebugMessage("Ready for charging if held");
            }

            return this;
        } else {
            context.getKeyData().rapidClickCount = 0;
            context.getKeyData().clearTimestamps();
            
            if (context.getConfig().disableNormalClick && context.getConfig().enableCharging) {
                context.showDebugMessage("Entering charging state directly (normal click disabled)");
                context.setLastSentPower(0f);
                return new ChargingState();
            }
            
            if (context.getConfig().enableCharging) {
                context.showDebugMessage("Ready for charging if held");
            }
            
            return this;
        }
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        if (context.getPressStartTime() > 0) {
            long holdDuration = currentTime - context.getPressStartTime();

            if (context.getConfig().disableNormalClick) {
                context.showDebugMessage("Normal click disabled by config");
                context.setPressStartTime(0);
                return this;
            }
            
            context.showDebugMessage("Key Press and Release detected - treating as normal click");
            context.setPressedTriggered(true);
            context.setPressStartTime(0);

            if (context.getConfig().pressCooldown > 0) {
                context.getKeyData().startCooldown(context.getConfig().pressCooldown);
                context.showDebugMessage("Started press cooldown: " +
                        context.getConfig().pressCooldown + "ms");
                return new CooldownState();
            }

            return new PressedState();
        }

        context.setPressStartTime(0);
        return this;
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        if (context.isPressed() && context.getPressStartTime() > 0) {
            long holdDuration = currentTime - context.getPressStartTime();

            if (context.getConfig().enableCharging) {
                if (holdDuration > context.getConfig().chargingTimeTolerance) {
                    context.showDebugMessage("Start Charging (duration: " + holdDuration + "ms)");
                    context.setLastSentPower(0f);
                    return new ChargingState();
                }
            }

            if (holdDuration >= context.getConfig().pressTimeTolerance &&
                    !context.isPressedTriggered() &&
                    !context.getConfig().enableCharging) {
                if (context.getConfig().disableNormalClick) {
                    return this;
                }
                
                context.setPressedTriggered(true);
                context.showDebugMessage("Key Pressed (normal hold)");
                return new PressedState();
            }
        } else if (!context.isPressed() && context.getPressStartTime() > 0 && 
                !context.isPressedTriggered() && 
                !context.getConfig().disableNormalClick) {
            context.showDebugMessage("Detecting missed click in tick - processing now");
            context.setPressedTriggered(true);
            context.setPressStartTime(0);
            return new PressedState();
        }

        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.IDLE;
    }
}