package org.crychicteam.cibrary.content.event;

public record ItemHurtEffectResult(boolean cancelled, int damage, boolean destroyed) {

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