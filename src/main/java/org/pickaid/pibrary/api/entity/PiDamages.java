package org.pickaid.pibrary.api.entity;

import java.util.Objects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Damage helpers for code that wants vanilla-compatible calculations without
 * duplicating Minecraft's armor formula.
 */
public final class PiDamages {
    private PiDamages() {
    }

    public static float afterArmor(float amount, float armor, float toughness) {
        if (amount <= 0.0F) {
            return 0.0F;
        }
        return CombatRules.getDamageAfterAbsorb(
                amount,
                nonNegative(armor, "armor"),
                nonNegative(toughness, "toughness"));
    }

    public static float afterArmorPenetration(
            float amount,
            float armor,
            float toughness,
            PiArmorPenetration penetration
    ) {
        Objects.requireNonNull(penetration, "penetration");
        return afterArmor(amount, penetration.reduceArmor(armor), toughness);
    }

    public static float afterArmorPenetration(
            LivingEntity target,
            float amount,
            PiArmorPenetration penetration
    ) {
        Objects.requireNonNull(target, "target");
        return afterArmorPenetration(
                amount,
                target.getArmorValue(),
                (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS),
                penetration);
    }

    public static float beforeArmorForFinalDamage(float finalAmount, float armor, float toughness) {
        if (finalAmount <= 0.0F) {
            return 0.0F;
        }
        float checkedArmor = nonNegative(armor, "armor");
        float checkedToughness = nonNegative(toughness, "toughness");
        if (checkedArmor <= 0.0F) {
            return finalAmount;
        }

        float low = 0.0F;
        float high = Math.max(finalAmount, 1.0F);
        while (afterArmor(high, checkedArmor, checkedToughness) < finalAmount && high < Float.MAX_VALUE / 2.0F) {
            high *= 2.0F;
        }
        for (int i = 0; i < 32; i++) {
            float mid = (low + high) * 0.5F;
            if (afterArmor(mid, checkedArmor, checkedToughness) < finalAmount) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return high;
    }

    /**
     * Returns the amount that should be written into a pre-armor hurt event so
     * vanilla's later armor pass produces the same result as reduced effective
     * armor would have produced.
     */
    public static float beforeArmorForArmorPenetration(
            float amount,
            float armor,
            float toughness,
            PiArmorPenetration penetration
    ) {
        float finalAmount = afterArmorPenetration(amount, armor, toughness, penetration);
        return beforeArmorForFinalDamage(finalAmount, armor, toughness);
    }

    /**
     * Calculates the pre-armor amount to write during Forge's
     * {@link LivingHurtEvent} stage when the desired result is vanilla's armor
     * pass applied with reduced effective armor.
     */
    public static float calculatePiercingCompensation(
            float amount,
            float armor,
            float toughness,
            PiArmorPenetration penetration
    ) {
        return beforeArmorForArmorPenetration(amount, armor, toughness, penetration);
    }

    /**
     * Applies armor penetration during {@link LivingHurtEvent} by writing a
     * compensated pre-armor amount back to the event.
     */
    public static float applyArmorPenetration(
            LivingHurtEvent event,
            float armor,
            float toughness,
            PiArmorPenetration penetration
    ) {
        Objects.requireNonNull(event, "event");
        float finalAmount = afterArmorPenetration(event.getAmount(), armor, toughness, penetration);
        float result = event.getSource() != null && event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)
                ? finalAmount
                : beforeArmorForFinalDamage(finalAmount, armor, toughness);
        event.setAmount(result);
        return result;
    }

    /**
     * Applies armor penetration using the target's current vanilla armor and
     * armor toughness values.
     */
    public static float applyArmorPenetration(
            LivingHurtEvent event,
            PiArmorPenetration penetration
    ) {
        Objects.requireNonNull(event, "event");
        LivingEntity target = event.getEntity();
        float armor = target.getArmorValue();
        float toughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float finalAmount = afterArmorPenetration(event.getAmount(), armor, toughness, penetration);
        float result = event.getSource() != null && event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)
                ? finalAmount
                : beforeArmorForFinalDamage(finalAmount, armor, toughness);
        event.setAmount(result);
        return result;
    }

    public static boolean hurtIgnoringCooldown(LivingEntity target, DamageSource source, float amount) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(source, "source");
        int previousInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        try {
            return target.hurt(source, amount);
        } finally {
            target.invulnerableTime = previousInvulnerableTime;
        }
    }

    private static float nonNegative(float value, String name) {
        if (!Float.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
        if (value < 0.0F) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }
}
