package org.pickaid.pibrary.content.key.state;

import org.pickaid.pibrary.api.key.KeyState;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;

/**
 * 表示充能后释放的状态
 * 这个状态表示键充满能量后被释放
 * 此状态需要特殊处理以确保服务器能正确接收
 */
public class HeldReleasedState implements KeyState {
    // 记录是否已经尝试过发送HELD_RELEASED状态
    private boolean hasTriedToSendUpdate = false;
    
    @Override
    public KeyState handlePress(KeyStateMachine context, long currentTime) {
        // 如果在冷却中，忽略按键
        if (context.getKeyData().inCooldown) {
            context.showDebugMessage("Press ignored during HELD_RELEASED cooldown");
            return this;
        }
        
        // 重置状态并处理新的按键
        context.showDebugMessage("New press while in HELD_RELEASED state - transitioning to IdleState");
        context.setPressedTriggered(false);
        context.setPressStartTime(0);
        
        // 确保网络状态正确同步
        context.getKeyData().state = KeyData.KeyState.IDLE;
        context.sendNetworkUpdate();
        context.forceLastSentState(KeyData.KeyState.IDLE);
        
        return new IdleState().handlePress(context, currentTime);
    }

    @Override
    public KeyState handleRelease(KeyStateMachine context, long currentTime) {
        if (context.getLastSentState() != KeyData.KeyState.HELD_RELEASED && !hasTriedToSendUpdate) {
            context.showDebugMessage(">>>>> Re-sending HELD_RELEASED state - CRITICAL UPDATE <<<<<");
            context.getKeyData().state = KeyData.KeyState.HELD_RELEASED;
            context.sendNetworkUpdate();
            context.forceLastSentState(KeyData.KeyState.HELD_RELEASED);
            hasTriedToSendUpdate = true;
        } else if (hasTriedToSendUpdate) {
            context.showDebugMessage("Already tried to send HELD_RELEASED, won't try again");
        } else {
            context.showDebugMessage("HELD_RELEASED already sent to server");
        }
        return this;
    }

    @Override
    public KeyState handleTick(KeyStateMachine context, long currentTime) {
        if (context.getLastSentState() != KeyData.KeyState.HELD_RELEASED && !hasTriedToSendUpdate) {
            context.showDebugMessage(">>>>> Tick checking - sending HELD_RELEASED state <<<<<");
            context.getKeyData().state = KeyData.KeyState.HELD_RELEASED;
            context.sendNetworkUpdate();
            context.forceLastSentState(KeyData.KeyState.HELD_RELEASED);
            hasTriedToSendUpdate = true;
        }

        if (context.getKeyData().inCooldown) {
            context.showDebugMessage(String.format("HELD_RELEASED cooldown: %d ms remaining", 
                    context.getKeyData().getRemainingCooldown(currentTime)));
        }

        if (!context.getKeyData().inCooldown) {
            context.showDebugMessage("HELD_RELEASED cooldown complete - returning to IDLE");

            context.getKeyData().state = KeyData.KeyState.IDLE;
            context.sendNetworkUpdate();
            context.forceLastSentState(KeyData.KeyState.IDLE);
            
            return new IdleState();
        }
        return this;
    }

    @Override
    public KeyData.KeyState getKeyDataState() {
        return KeyData.KeyState.HELD_RELEASED;
    }

    public KeyState reset(KeyStateMachine context) {
        context.showDebugMessage("Manually resetting HELD_RELEASED state");
        context.getKeyData().inCooldown = false;
        context.getKeyData().state = KeyData.KeyState.IDLE;
        context.sendNetworkUpdate();
        context.forceLastSentState(KeyData.KeyState.IDLE);
        return new IdleState();
    }
}