package org.pickaid.pibrary.api.entity;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;

/**
 * Global accessor for the installed entity spatial index.
 */
public final class PiEntitySpatialServices {
    public static final PibraryServiceKey<PiEntitySpatialIndex> KEY =
            new PibraryServiceKey<>(Pibrary.id("entity_spatial_index"), PiEntitySpatialIndex.class);

    private PiEntitySpatialServices() {
    }

    /**
     * Installs the global spatial index.
     *
     * @param service service instance or {@code null} to remove
     */
    public static void install(PiEntitySpatialIndex service) {
        PibraryServices.install(KEY, service);
    }

    /**
     * Finds the global spatial index.
     *
     * @return installed index, if present
     */
    public static Optional<PiEntitySpatialIndex> find() {
        return PibraryServices.findService(KEY);
    }

    /**
     * Requires the global spatial index.
     *
     * @return installed spatial index
     */
    public static PiEntitySpatialIndex require() {
        return PibraryServices.requireService(KEY);
    }

    /**
     * Executes a spatial query using the global index.
     *
     * @param level source level
     * @param bounds query volume
     * @param filter candidate filter
     * @return immutable query result
     */
    public static List<Entity> query(Level level, AABB bounds, Predicate<Entity> filter) {
        return require().query(level, bounds, filter);
    }
}
