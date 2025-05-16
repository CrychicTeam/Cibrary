package org.pickaid.pibrary.content.logic.ticker.entity;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SkillExecutor extends LivingEntityTracker {
    private final double basePower;
    private final int distance;

    private BiFunction<LivingEntity, SkillExecutor, Boolean> castConditionCheck;
    private BiFunction<LivingEntity, SkillExecutor, Boolean> resourceConsumer;
    private BiConsumer<LivingEntity, SkillExecutor> castEffect;
    private BiConsumer<LivingEntity, SkillExecutor> finalEffect;
    private Function<Integer, Double> powerCalculator;

    private boolean skillActive = false;

    public SkillExecutor(LivingEntity caster, int duration, double basePower, int distance) {
        super(caster, duration, false);
        this.basePower = basePower;
        this.distance = distance;
        this.powerCalculator = (tick) -> basePower;
    }

    public SkillExecutor setCastCondition(BiFunction<LivingEntity, SkillExecutor, Boolean> condition) {
        this.castConditionCheck = condition;
        return this;
    }

    public SkillExecutor setResourceConsumer(BiFunction<LivingEntity, SkillExecutor, Boolean> consumer) {
        this.resourceConsumer = consumer;
        return this;
    }

    public SkillExecutor setCastEffect(BiConsumer<LivingEntity, SkillExecutor> effect) {
        this.castEffect = effect;
        return this;
    }

    public SkillExecutor setFinalEffect(BiConsumer<LivingEntity, SkillExecutor> effect) {
        this.finalEffect = effect;
        return this;
    }

    public SkillExecutor setPowerCalculator(Function<Integer, Double> calculator) {
        this.powerCalculator = calculator;
        return this;
    }

    public double getCurrentPower() {
        return powerCalculator != null ? powerCalculator.apply(tickCount) : basePower;
    }

    public double getBasePower() {
        return basePower;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isSkillActive() {
        return skillActive;
    }

    public void setSkillActive(boolean active) {
        this.skillActive = active;
    }

    @Override
    public boolean getAsBoolean() {
        LivingEntity caster = findTargetEntity();
        if (caster == null) {
            if (finalEffect != null && isSkillActive()) {
                try {
                    finalEffect.accept(null, this);
                } catch (Exception ignored) {
                }
            }
            return true;
        }

        try {
            boolean timeUp = duration >= 0 && tickCount >= duration;
            if (timeUp) {
                if (finalEffect != null && isSkillActive()) {
                    finalEffect.accept(caster, this);
                }
                return true;
            }
            boolean canCast = castConditionCheck == null || castConditionCheck.apply(caster, this);
            if (canCast) {
                boolean resourceConsumed = resourceConsumer == null || resourceConsumer.apply(caster, this);
                if (resourceConsumed && castEffect != null) {
                    setSkillActive(true);
                    castEffect.accept(caster, this);
                } else if (isSkillActive()) {
                    if (finalEffect != null) {
                        finalEffect.accept(caster, this);
                    }
                    setSkillActive(false);
                }
            } else if (isSkillActive()) {
                if (finalEffect != null) {
                    finalEffect.accept(caster, this);
                }
                setSkillActive(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (finalEffect != null && isSkillActive()) {
                try {
                    finalEffect.accept(caster, this);
                } catch (Exception ignored) {}
            }
            return true;
        }

        return super.getAsBoolean();
    }

    public static SkillExecutor start(
            LivingEntity caster,
            int duration,
            double power,
            int distance,
            BiFunction<LivingEntity, SkillExecutor, Boolean> condition,
            BiFunction<LivingEntity, SkillExecutor, Boolean> consumer,
            BiConsumer<LivingEntity, SkillExecutor> effect,
            BiConsumer<LivingEntity, SkillExecutor> finalEffect,
            Function<Integer, Double> powerCalculator) {

        SkillExecutor executor = new SkillExecutor(caster, duration, power, distance);

        if (condition != null) executor.setCastCondition(condition);
        if (consumer != null) executor.setResourceConsumer(consumer);
        if (effect != null) executor.setCastEffect(effect);
        if (finalEffect != null) executor.setFinalEffect(finalEffect);
        if (powerCalculator != null) executor.setPowerCalculator(powerCalculator);

        executor.start();
        return executor;
    }
}