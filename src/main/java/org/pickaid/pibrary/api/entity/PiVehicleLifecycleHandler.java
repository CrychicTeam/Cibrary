package org.pickaid.pibrary.api.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Receives mount and dismount callbacks for entities matching a registered type.
 *
 * @param <E> supported entity type
 */
public interface PiVehicleLifecycleHandler<E extends Entity> {
    /**
     * Called when the tracked entity mounts a vehicle.
     *
     * @param entity mounting entity
     * @param vehicle mounted vehicle
     * @param level level where the event happened
     */
    default void onMount(E entity, @Nullable Entity vehicle, Level level) {
    }

    /**
     * Called when the tracked entity dismounts a vehicle.
     *
     * @param entity dismounting entity
     * @param vehicle previous vehicle
     * @param level level where the event happened
     */
    default void onDismount(E entity, @Nullable Entity vehicle, Level level) {
    }
}
