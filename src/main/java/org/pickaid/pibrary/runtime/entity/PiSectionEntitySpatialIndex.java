package org.pickaid.pibrary.runtime.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.phys.AABB;
import org.pickaid.pibrary.api.entity.PiEntitySpatialIndex;
import org.pickaid.pibrary.mixin.PersistentEntitySectionManagerAccessor;
import org.pickaid.pibrary.mixin.ServerLevelAccessor;

/**
 * Server-side spatial index that reads Minecraft's entity section storage
 * directly through mixin accessors and falls back to vanilla queries otherwise.
 */
public final class PiSectionEntitySpatialIndex implements PiEntitySpatialIndex {
    private final PiVanillaEntitySpatialIndex fallback = new PiVanillaEntitySpatialIndex();

    @Override
    public List<Entity> query(Level level, AABB bounds, Predicate<Entity> filter) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(bounds, "bounds");
        Objects.requireNonNull(filter, "filter");
        if (!(level instanceof ServerLevel serverLevel)) {
            return fallback.query(level, bounds, filter);
        }

        List<Entity> entities = new ArrayList<>();
        EntitySectionStorage<Entity> storage = sectionStorage(serverLevel);
        storage.forEachAccessibleNonEmptySection(bounds, section -> section.getEntities(bounds, entity -> {
            if (filter.test(entity)) {
                entities.add(entity);
            }
            return AbortableIterationConsumer.Continuation.CONTINUE;
        }));
        return List.copyOf(entities);
    }

    @SuppressWarnings("unchecked")
    private EntitySectionStorage<Entity> sectionStorage(ServerLevel level) {
        return ((PersistentEntitySectionManagerAccessor<Entity>) ((ServerLevelAccessor) level).getEntityManager()).getSectionStorage();
    }
}
