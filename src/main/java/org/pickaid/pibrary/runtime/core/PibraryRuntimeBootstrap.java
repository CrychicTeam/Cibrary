package org.pickaid.pibrary.runtime.core;

import org.pickaid.pibrary.api.entity.PiEntitySpatialIndex;
import org.pickaid.pibrary.api.entity.PiEntityLifecycles;
import org.pickaid.pibrary.api.entity.PiEntitySpatialIndexes;
import org.pickaid.pibrary.api.entity.PiVehicleLifecycles;
import org.pickaid.pibrary.api.facet.PiLevelFacetStorages;
import org.pickaid.pibrary.api.projectile.PiProjectileImpacts;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycles;
import org.pickaid.pibrary.api.projectile.PiProjectileTraces;
import org.pickaid.pibrary.api.targeting.PiTargeting;
import org.pickaid.pibrary.runtime.entity.PiDefaultEntityLifecycleRegistry;
import org.pickaid.pibrary.runtime.entity.PiDefaultVehicleLifecycleRegistry;
import org.pickaid.pibrary.runtime.facet.PiSavedDataLevelFacetStorage;
import org.pickaid.pibrary.runtime.entity.PiSectionEntitySpatialIndex;
import org.pickaid.pibrary.runtime.projectile.PiDefaultProjectileImpactRegistry;
import org.pickaid.pibrary.runtime.projectile.PiDefaultProjectileLifecycleRegistry;
import org.pickaid.pibrary.runtime.projectile.PiDefaultProjectileTracer;
import org.pickaid.pibrary.runtime.targeting.PiDefaultTargetingResolver;

/**
 * Installs default runtime implementations for the core Pibrary systems when a
 * custom override has not already been provided.
 */
public final class PibraryRuntimeBootstrap {
    private PibraryRuntimeBootstrap() {
    }

    /**
     * Bootstraps the default runtime graph.
     */
    public static void bootstrap() {
        PiEntityLifecycles.find().orElseGet(() -> {
            PiEntityLifecycles.install(new PiDefaultEntityLifecycleRegistry());
            return PiEntityLifecycles.require();
        });
        PiVehicleLifecycles.find().orElseGet(() -> {
            PiVehicleLifecycles.install(new PiDefaultVehicleLifecycleRegistry());
            return PiVehicleLifecycles.require();
        });
        PiEntitySpatialIndex spatialIndex = PiEntitySpatialIndexes.find().orElseGet(() -> {
            PiEntitySpatialIndex created = new PiSectionEntitySpatialIndex();
            PiEntitySpatialIndexes.install(created);
            return created;
        });
        PiTargeting.find().orElseGet(() -> {
            PiTargeting.install(new PiDefaultTargetingResolver(spatialIndex));
            return PiTargeting.require();
        });
        PiProjectileLifecycles.find().orElseGet(() -> {
            PiProjectileLifecycles.install(new PiDefaultProjectileLifecycleRegistry());
            return PiProjectileLifecycles.require();
        });
        PiProjectileImpacts.find().orElseGet(() -> {
            PiProjectileImpacts.install(new PiDefaultProjectileImpactRegistry());
            return PiProjectileImpacts.require();
        });
        PiProjectileTraces.find().orElseGet(() -> {
            PiProjectileTraces.install(new PiDefaultProjectileTracer(spatialIndex));
            return PiProjectileTraces.require();
        });
        PiLevelFacetStorages.find().orElseGet(() -> {
            PiLevelFacetStorages.install(new PiSavedDataLevelFacetStorage());
            return PiLevelFacetStorages.require();
        });
    }
}
