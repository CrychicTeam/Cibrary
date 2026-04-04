package org.pickaid.pibrary.api.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface PiLivingHurtHandler<E extends LivingEntity> {
    float onHurt(E entity, DamageSource source, float amount);
}
