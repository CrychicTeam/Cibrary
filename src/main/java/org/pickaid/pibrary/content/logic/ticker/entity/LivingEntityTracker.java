package org.pickaid.pibrary.content.logic.ticker.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;

import java.util.function.Consumer;

public class LivingEntityTracker extends EntityTracker<LivingEntity> {

    public LivingEntityTracker(LivingEntity entity, int duration, boolean isClient) {
        super(entity, duration, isClient);
    }

    @Override
    protected LivingEntity findTargetEntity() {
        if (entity != null && entity.isAlive() && !entity.isRemoved()) {
            return entity;
        }
        return null;
    }

    public LivingEntityTracker addStatusEffect(MobEffect effect, int effectDuration, int amplifier) {
        Consumer<LivingEntity> currentHandler = this.effectHandler;

        this.effectHandler = entity -> {
            // 如果每秒执行一次效果
            if (tickCount % 20 == 0) {
                entity.addEffect(new MobEffectInstance(effect, effectDuration, amplifier, false, true));
            }

            // 保留之前的处理器
            if (currentHandler != null) {
                currentHandler.accept(entity);
            }
        };

        return this;
    }

    public LivingEntityTracker addPeriodicDamage(float damagePerSecond, DamageSource damageSource) {
        Consumer<LivingEntity> currentHandler = this.effectHandler;
        this.effectHandler = entity -> {
            if (tickCount % 20 == 0) {
                entity.hurt(damageSource, damagePerSecond);
            }
            if (currentHandler != null) {
                currentHandler.accept(entity);
            }
        };
        return this;
    }

    public static LivingEntityTracker start(
            LivingEntity entity, int duration, boolean isClient, Consumer<LivingEntity> handler) {

        LivingEntityTracker tracker = new LivingEntityTracker(entity, duration, isClient);

        if (handler != null) {
            tracker.setEffectHandler(handler);
        }

        tracker.start();
        return tracker;
    }
}