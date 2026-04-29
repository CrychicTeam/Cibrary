package org.pickaid.pibrary.api.entity;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Queries entities inside a world-space volume.
 */
public interface PiEntitySpatialIndex {
    /**
     * Returns entities inside the requested bounds that satisfy the provided filter.
     *
     * @param level source level
     * @param bounds query volume
     * @param filter candidate filter
     * @return immutable query result
     */
    List<Entity> query(Level level, AABB bounds, Predicate<Entity> filter);
}
