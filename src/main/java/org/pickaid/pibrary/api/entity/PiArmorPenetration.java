package org.pickaid.pibrary.api.entity;

import net.minecraft.util.Mth;

/**
 * Armor penetration values used before vanilla armor reduction is calculated.
 *
 * <p>The ratio is applied first, then the flat value is subtracted. For example,
 * {@code of(5, 0.5)} turns 20 armor into {@code 20 * 0.5 - 5 = 5} effective
 * armor. The final armor value never drops below zero.</p>
 *
 * @param flat flat armor points ignored after ratio penetration
 * @param ratio fraction of armor ignored before flat penetration, clamped to {@code [0, 1]}
 */
public record PiArmorPenetration(float flat, float ratio) {
    public PiArmorPenetration {
        flat = nonNegative(flat, "flat");
        ratio = Mth.clamp(finite(ratio, "ratio"), 0.0F, 1.0F);
    }

    public static PiArmorPenetration none() {
        return new PiArmorPenetration(0.0F, 0.0F);
    }

    public static PiArmorPenetration flat(float value) {
        return new PiArmorPenetration(value, 0.0F);
    }

    public static PiArmorPenetration ratio(float value) {
        return new PiArmorPenetration(0.0F, value);
    }

    public static PiArmorPenetration of(float flat, float ratio) {
        return new PiArmorPenetration(flat, ratio);
    }

    public float reduceArmor(float armor) {
        float checkedArmor = nonNegative(armor, "armor");
        return Math.max(0.0F, checkedArmor * (1.0F - ratio) - flat);
    }

    private static float nonNegative(float value, String name) {
        float checked = finite(value, name);
        if (checked < 0.0F) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return checked;
    }

    private static float finite(float value, String name) {
        if (!Float.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
        return value;
    }
}
