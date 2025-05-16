package org.pickaid.pibrary.content.key.timing;

/**
 * 管理键位状态机的所有时间相关变量
 */
public class KeyTimingTracker {
    // 按键时间点记录
    private long pressStartTime = 0;
    private long heldStartTime = 0;
    private long chargedReleaseTime = 0;
    private long rapidClickCooldownEndTime = 0;
    
    // 按键输入状态跟踪
    private boolean previousPressed = false;
    private boolean isCurrentlyPressed = false;
    
    // 状态标记
    private boolean inTimeoutState = false;
    private boolean pressedTriggered = false;
    
    /**
     * 重置所有状态（除了当前是否按下）
     */
    public void resetState() {
        pressedTriggered = false;
        inTimeoutState = false;
        
        if (!isCurrentlyPressed) {
            pressStartTime = 0;
            heldStartTime = 0;
        }
    }
    
    /**
     * 更新当前按键状态
     */
    public void updatePressState(boolean isPressed) {
        previousPressed = isCurrentlyPressed;
        isCurrentlyPressed = isPressed;
    }
    
    /**
     * 检查是否刚刚按下
     */
    public boolean isJustPressed() {
        return isCurrentlyPressed && !previousPressed;
    }
    
    /**
     * 检查是否刚刚释放
     */
    public boolean isJustReleased() {
        return !isCurrentlyPressed && previousPressed;
    }
    
    /**
     * 获取当前是否按下
     */
    public boolean isPressed() {
        return isCurrentlyPressed;
    }
    
    /**
     * 获取上一次是否按下
     */
    public boolean wasPreviousPressed() {
        return previousPressed;
    }
    
    /**
     * 获取按键开始时间
     */
    public long getPressStartTime() {
        return pressStartTime;
    }
    
    /**
     * 设置按键开始时间
     */
    public void setPressStartTime(long time) {
        pressStartTime = time;
    }
    
    /**
     * 获取充能持续时间
     */
    public long getHoldDuration(long currentTime) {
        return pressStartTime > 0 ? currentTime - pressStartTime : 0;
    }
    
    /**
     * 获取保持满充能开始时间
     */
    public long getHeldStartTime() {
        return heldStartTime;
    }
    
    /**
     * 设置保持满充能开始时间
     */
    public void setHeldStartTime(long time) {
        heldStartTime = time;
    }
    
    /**
     * 获取充能释放时间
     */
    public long getChargedReleaseTime() {
        return chargedReleaseTime;
    }
    
    /**
     * 设置充能释放时间
     */
    public void setChargedReleaseTime(long time) {
        chargedReleaseTime = time;
    }
    
    /**
     * 检查是否处于超时状态
     */
    public boolean isInTimeoutState() {
        return inTimeoutState;
    }
    
    /**
     * 设置超时状态
     */
    public void setInTimeoutState(boolean inTimeout) {
        inTimeoutState = inTimeout;
    }
    
    /**
     * 检查是否已触发按下事件
     */
    public boolean isPressedTriggered() {
        return pressedTriggered;
    }
    
    /**
     * 设置按下事件已触发
     */
    public void setPressedTriggered(boolean triggered) {
        pressedTriggered = triggered;
    }
    
    /**
     * 开始快速点击冷却
     */
    public void startRapidClickCooldown(long duration, long currentTime) {
        if (duration <= 0) return;
        rapidClickCooldownEndTime = currentTime + duration;
    }
    
    /**
     * 检查快速点击冷却状态
     * @return true表示仍在冷却中，false表示冷却结束
     */
    public boolean checkRapidClickCooldown(long currentTime) {
        if (rapidClickCooldownEndTime <= 0) return false;
        
        if (currentTime >= rapidClickCooldownEndTime) {
            rapidClickCooldownEndTime = 0;
            return false;
        }
        
        return true;
    }
} 