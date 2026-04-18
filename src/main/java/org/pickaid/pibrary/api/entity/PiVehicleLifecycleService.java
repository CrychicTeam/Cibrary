package org.pickaid.pibrary.api.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Dispatches vehicle lifecycle events to handlers registered by entity type.
 */
public interface PiVehicleLifecycleService {
    /**
     * Registers a handler for the given entity type or any of its subclasses.
     *
     * @param entityType root entity type to match
     * @param handler lifecycle handler
     * @param <E> supported entity type
     */
    <E extends Entity> void register(Class<E> entityType, PiVehicleLifecycleHandler<? super E> handler);

    /**
     * Dispatches a mount event.
     *
     * @param entity mounting entity
     * @param vehicle vehicle being mounted
     * @param level level where the mount happened
     */
    void onMount(Entity entity, @Nullable Entity vehicle, Level level);

    /**
     * Dispatches a dismount event.
     *
     * @param entity dismounting entity
     * @param vehicle vehicle being left
     * @param level level where the dismount happened
     */
    void onDismount(Entity entity, @Nullable Entity vehicle, Level level);
}
