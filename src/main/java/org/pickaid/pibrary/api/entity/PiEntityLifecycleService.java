package org.pickaid.pibrary.api.entity;

import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Dispatches entity lifecycle events to handlers registered by entity type.
 */
public interface PiEntityLifecycleService {
    /**
     * Registers a handler for the given entity type or any of its subclasses.
     *
     * @param entityType root entity type to match
     * @param handler lifecycle handler
     * @param <E> supported entity type
     */
    <E extends Entity> void register(Class<E> entityType, PiEntityLifecycleHandler<? super E> handler);

    /**
     * Dispatches an entity-join event.
     *
     * @param entity entity that joined the level
     * @param level target level
     * @param loadedFromDisk whether the entity came from disk
     */
    void onJoinLevel(Entity entity, Level level, boolean loadedFromDisk);

    /**
     * Dispatches an entity section-transition event.
     *
     * @param entity moved entity
     * @param oldSection previous section
     * @param newSection new section
     */
    void onEnterSection(Entity entity, SectionPos oldSection, SectionPos newSection);
}
