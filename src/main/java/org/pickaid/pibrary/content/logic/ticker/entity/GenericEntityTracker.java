package org.pickaid.pibrary.content.logic.ticker.entity;

import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public class GenericEntityTracker extends EntityTracker<Entity> {

    public GenericEntityTracker(Entity entity, int duration, boolean isClient) {
        super(entity, duration, isClient);
    }

    @Override
    protected Entity findTargetEntity() {
        if (entity != null && entity.isAlive() && !entity.isRemoved()) {
            return entity;
        }
        return null;
    }

    @Override
    protected void handleMissingEntity() {
        super.handleMissingEntity();
    }

    public static <E extends Entity> GenericEntityTracker start(
            E entity, int duration, boolean isClient, Consumer<Entity> handler) {
        GenericEntityTracker tracker = new GenericEntityTracker(entity, duration, isClient);
        if (handler != null) {
            tracker.setEffectHandler(handler);
        }

        tracker.start();
        return tracker;
    }
}