package org.pickaid.pibrary.content.logic.ticker.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.pickaid.pibrary.content.logic.ticker.BaseTicker;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class EntityTracker<T extends Entity> implements BooleanSupplier {
    protected final T entity;
    protected final int duration;
    protected final boolean isClient;

    protected int tickCount = 0;
    protected Vec3 lastKnownPosition;

    protected Consumer<T> effectHandler;
    protected Predicate<T> continuationCheck;
    protected Consumer<Vec3> missingEntityHandler;
    protected int maxMissingTicks = 0;
    protected int missingTicks = 0;

    public EntityTracker(T entity, int duration, boolean isClient) {
        this.entity = entity;
        this.duration = duration;
        this.isClient = isClient;
        this.lastKnownPosition = entity.position();
    }

    public EntityTracker<T> setEffectHandler(Consumer<T> handler) {
        this.effectHandler = handler;
        return this;
    }

    public EntityTracker<T> setContinuationCheck(Predicate<T> check) {
        this.continuationCheck = check;
        return this;
    }

    public EntityTracker<T> setMissingEntityHandler(Consumer<Vec3> handler, int maxTicks) {
        this.missingEntityHandler = handler;
        this.maxMissingTicks = maxTicks;
        return this;
    }

    public void start() {
        BaseTicker.scheduleTask(this, 1, isClient);
    }

    public void cancel() {
        BaseTicker.cancelTask(this);
    }

    protected abstract T findTargetEntity();

    protected void handleMissingEntity() {
        missingTicks++;
        if (missingEntityHandler != null) {
            missingEntityHandler.accept(lastKnownPosition);
        }
    }

    @Override
    public boolean getAsBoolean() {
        tickCount++;
        T currentEntity = findTargetEntity();
        if (currentEntity != null) {
            missingTicks = 0;
            lastKnownPosition = currentEntity.position();
            if (effectHandler != null) {
                effectHandler.accept(currentEntity);
            }
            if (continuationCheck != null && !continuationCheck.test(currentEntity)) {
                return true;
            }
        } else {
            handleMissingEntity();
            if (maxMissingTicks >= 0 && missingTicks > maxMissingTicks) {
                return true;
            }
        }

        return duration >= 0 && tickCount >= duration;
    }
}