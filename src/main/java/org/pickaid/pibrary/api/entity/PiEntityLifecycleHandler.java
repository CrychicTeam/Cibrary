package org.pickaid.pibrary.api.entity;

import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Receives lifecycle callbacks for entities matching a registered type.
 *
 * @param <E> supported entity type
 */
public interface PiEntityLifecycleHandler<E extends Entity> {
    /**
     * Called when a matching entity joins a level.
     *
     * @param entity matching entity
     * @param level target level
     * @param loadedFromDisk whether the entity came from saved data instead of a live spawn
     */
    default void onJoinLevel(E entity, Level level, boolean loadedFromDisk) {
    }

    /**
     * Called when a matching entity crosses section boundaries.
     *
     * @param entity matching entity
     * @param oldSection previous section
     * @param newSection new section
     */
    default void onEnterSection(E entity, SectionPos oldSection, SectionPos newSection) {
    }
}
