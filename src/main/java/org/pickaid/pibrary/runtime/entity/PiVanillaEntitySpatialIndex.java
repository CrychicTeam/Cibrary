package org.pickaid.pibrary.runtime.entity;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.pickaid.pibrary.api.entity.PiEntitySpatialIndex;

/**
 * Vanilla-backed spatial index that delegates directly to level entity queries.
 */
public final class PiVanillaEntitySpatialIndex implements PiEntitySpatialIndex {
    @Override
    public List<Entity> query(Level level, AABB bounds, Predicate<Entity> filter) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(bounds, "bounds");
        Objects.requireNonNull(filter, "filter");
        return List.copyOf(level.getEntities((Entity) null, bounds, filter::test));
    }
}
