package org.pickaid.pibrary.api.entity;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public interface PiEntitySpatialIndex {
    List<Entity> query(Level level, AABB bounds, Predicate<Entity> filter);
}
