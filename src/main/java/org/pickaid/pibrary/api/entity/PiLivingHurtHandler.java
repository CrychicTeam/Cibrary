package org.pickaid.pibrary.api.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Functional hook for adjusting hurt amounts on living entities.
 *
 * @param <E> supported living entity type
 */
@FunctionalInterface
public interface PiLivingHurtHandler<E extends LivingEntity> {
    /**
     * Handles a hurt event and returns the final damage amount.
     *
     * @param entity hurt entity
     * @param source damage source
     * @param amount incoming damage amount
     * @return final damage amount
     */
    float onHurt(E entity, DamageSource source, float amount);
}
