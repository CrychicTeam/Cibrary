package org.crychicteam.cibrary.content.event;

public class ItemHurtEffectResult {
    private final boolean cancelled;
    private final int damage;
    private final boolean destroyed;

    public ItemHurtEffectResult(boolean cancelled, int damage, boolean destroyed) {
        this.cancelled = cancelled;
        this.damage = damage;
        this.destroyed = destroyed;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public static ItemHurtEffectResult unmodified() {
        return new ItemHurtEffectResult(false, -1, false);
    }

    public static ItemHurtEffectResult cancel() {
        return new ItemHurtEffectResult(true, -1, false);
    }

    public static ItemHurtEffectResult setDamage(int damage) {
        return new ItemHurtEffectResult(false, damage, false);
    }

    public static ItemHurtEffectResult destroy() {
        return new ItemHurtEffectResult(false, -1, true);
    }
}