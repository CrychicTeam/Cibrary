package org.pickaid.pibrary.runtime.core;

import org.pickaid.pibrary.api.entity.PiEntitySpatialIndex;
import org.pickaid.pibrary.api.entity.PiEntityLifecycleServices;
import org.pickaid.pibrary.api.entity.PiEntitySpatialServices;
import org.pickaid.pibrary.api.entity.PiVehicleLifecycleServices;
import org.pickaid.pibrary.api.service.PiLevelServiceStorages;
import org.pickaid.pibrary.api.projectile.PiProjectileImpactServices;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycleServices;
import org.pickaid.pibrary.api.projectile.PiProjectileServices;
import org.pickaid.pibrary.api.targeting.PiTargetingServices;
import org.pickaid.pibrary.runtime.entity.PiDefaultEntityLifecycleService;
import org.pickaid.pibrary.runtime.entity.PiDefaultVehicleLifecycleService;
import org.pickaid.pibrary.runtime.level.PiSavedDataLevelServiceStorage;
import org.pickaid.pibrary.runtime.entity.PiVanillaEntitySpatialIndex;
import org.pickaid.pibrary.runtime.projectile.PiDefaultProjectileImpactService;
import org.pickaid.pibrary.runtime.projectile.PiDefaultProjectileLifecycleService;
import org.pickaid.pibrary.runtime.projectile.PiDefaultProjectileService;
import org.pickaid.pibrary.runtime.targeting.PiDefaultTargetingService;

/**
 * Installs default runtime implementations for the core Pibrary services when a
 * custom override has not already been provided.
 */
public final class PibraryRuntimeServices {
    private PibraryRuntimeServices() {
    }

    /**
     * Bootstraps the default runtime service graph.
     */
    public static void bootstrap() {
        PiEntityLifecycleServices.find().orElseGet(() -> {
            PiEntityLifecycleServices.install(new PiDefaultEntityLifecycleService());
            return PiEntityLifecycleServices.require();
        });
        PiVehicleLifecycleServices.find().orElseGet(() -> {
            PiVehicleLifecycleServices.install(new PiDefaultVehicleLifecycleService());
            return PiVehicleLifecycleServices.require();
        });
        PiEntitySpatialIndex spatialIndex = PiEntitySpatialServices.find().orElseGet(() -> {
            PiEntitySpatialIndex created = new PiVanillaEntitySpatialIndex();
            PiEntitySpatialServices.install(created);
            return created;
        });
        PiTargetingServices.find().orElseGet(() -> {
            PiTargetingServices.install(new PiDefaultTargetingService(spatialIndex));
            return PiTargetingServices.require();
        });
        PiProjectileLifecycleServices.find().orElseGet(() -> {
            PiProjectileLifecycleServices.install(new PiDefaultProjectileLifecycleService());
            return PiProjectileLifecycleServices.require();
        });
        PiProjectileImpactServices.find().orElseGet(() -> {
            PiProjectileImpactServices.install(new PiDefaultProjectileImpactService());
            return PiProjectileImpactServices.require();
        });
        PiProjectileServices.find().orElseGet(() -> {
            PiProjectileServices.install(new PiDefaultProjectileService(spatialIndex));
            return PiProjectileServices.require();
        });
        PiLevelServiceStorages.find().orElseGet(() -> {
            PiLevelServiceStorages.install(new PiSavedDataLevelServiceStorage());
            return PiLevelServiceStorages.require();
        });
    }
}
