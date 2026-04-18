package org.pickaid.pibrary.runtime.core;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.entity.PiEntityLifecycleService;
import org.pickaid.pibrary.api.entity.PiEntityLifecycleServices;
import org.pickaid.pibrary.api.entity.PiEntitySpatialIndex;
import org.pickaid.pibrary.api.entity.PiEntitySpatialServices;
import org.pickaid.pibrary.api.entity.PiVehicleLifecycleService;
import org.pickaid.pibrary.api.entity.PiVehicleLifecycleServices;
import org.pickaid.pibrary.api.service.PiLevelServiceStorage;
import org.pickaid.pibrary.api.service.PiLevelServiceStorages;
import org.pickaid.pibrary.api.projectile.PiProjectileImpactService;
import org.pickaid.pibrary.api.projectile.PiProjectileImpactServices;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycleService;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycleServices;
import org.pickaid.pibrary.api.projectile.PiProjectileService;
import org.pickaid.pibrary.api.projectile.PiProjectileServices;
import org.pickaid.pibrary.api.targeting.PiTargetingService;
import org.pickaid.pibrary.api.targeting.PiTargetingServices;
import org.pickaid.pibrary.runtime.entity.PiDefaultEntityLifecycleService;
import org.pickaid.pibrary.runtime.entity.PiDefaultVehicleLifecycleService;
import org.pickaid.pibrary.runtime.entity.PiVanillaEntitySpatialIndex;
import org.pickaid.pibrary.runtime.level.PiSavedDataLevelServiceStorage;
import org.pickaid.pibrary.runtime.projectile.PiDefaultProjectileImpactService;
import org.pickaid.pibrary.runtime.projectile.PiDefaultProjectileLifecycleService;
import org.pickaid.pibrary.runtime.projectile.PiDefaultProjectileService;
import org.pickaid.pibrary.runtime.targeting.PiDefaultTargetingService;

class PibraryRuntimeServicesTest {
    @Test
    void pibraryConstructorBootstrapsDefaultLevelServiceStorage() {
        PiLevelServiceStorage previousLevelStorage = PiLevelServiceStorages.find().orElse(null);
        try {
            PiLevelServiceStorages.install(null);

            new Pibrary();

            assertTrue(PiLevelServiceStorages.require() instanceof PiSavedDataLevelServiceStorage);
        } finally {
            PiLevelServiceStorages.install(previousLevelStorage);
        }
    }

    @Test
    void bootstrapInstallsDefaultRuntimeServicesWhenMissing() {
        PiEntityLifecycleService previousEntityLifecycle = PiEntityLifecycleServices.find().orElse(null);
        PiVehicleLifecycleService previousVehicleLifecycle = PiVehicleLifecycleServices.find().orElse(null);
        PiEntitySpatialIndex previousSpatial = PiEntitySpatialServices.find().orElse(null);
        PiTargetingService previousTargeting = PiTargetingServices.find().orElse(null);
        PiProjectileLifecycleService previousProjectileLifecycle = PiProjectileLifecycleServices.find().orElse(null);
        PiProjectileImpactService previousProjectileImpact = PiProjectileImpactServices.find().orElse(null);
        PiProjectileService previousProjectile = PiProjectileServices.find().orElse(null);
        PiLevelServiceStorage previousLevelStorage = PiLevelServiceStorages.find().orElse(null);
        try {
            PiEntityLifecycleServices.install(null);
            PiVehicleLifecycleServices.install(null);
            PiEntitySpatialServices.install(null);
            PiTargetingServices.install(null);
            PiProjectileLifecycleServices.install(null);
            PiProjectileImpactServices.install(null);
            PiProjectileServices.install(null);
            PiLevelServiceStorages.install(null);

            PibraryRuntimeServices.bootstrap();

            assertTrue(PiEntityLifecycleServices.require() instanceof PiDefaultEntityLifecycleService);
            assertTrue(PiVehicleLifecycleServices.require() instanceof PiDefaultVehicleLifecycleService);
            assertTrue(PiEntitySpatialServices.require() instanceof PiVanillaEntitySpatialIndex);
            assertTrue(PiTargetingServices.require() instanceof PiDefaultTargetingService);
            assertTrue(PiProjectileLifecycleServices.require() instanceof PiDefaultProjectileLifecycleService);
            assertTrue(PiProjectileImpactServices.require() instanceof PiDefaultProjectileImpactService);
            assertTrue(PiProjectileServices.require() instanceof PiDefaultProjectileService);
            assertTrue(PiLevelServiceStorages.require() instanceof PiSavedDataLevelServiceStorage);
        } finally {
            PiEntityLifecycleServices.install(previousEntityLifecycle);
            PiVehicleLifecycleServices.install(previousVehicleLifecycle);
            PiEntitySpatialServices.install(previousSpatial);
            PiTargetingServices.install(previousTargeting);
            PiProjectileLifecycleServices.install(previousProjectileLifecycle);
            PiProjectileImpactServices.install(previousProjectileImpact);
            PiProjectileServices.install(previousProjectile);
            PiLevelServiceStorages.install(previousLevelStorage);
        }
    }

    @Test
    void bootstrapKeepsCustomRuntimeOverrides() {
        PiEntityLifecycleService previousEntityLifecycle = PiEntityLifecycleServices.find().orElse(null);
        PiVehicleLifecycleService previousVehicleLifecycle = PiVehicleLifecycleServices.find().orElse(null);
        PiEntitySpatialIndex previousSpatial = PiEntitySpatialServices.find().orElse(null);
        PiTargetingService previousTargeting = PiTargetingServices.find().orElse(null);
        PiProjectileLifecycleService previousProjectileLifecycle = PiProjectileLifecycleServices.find().orElse(null);
        PiProjectileImpactService previousProjectileImpact = PiProjectileImpactServices.find().orElse(null);
        PiProjectileService previousProjectile = PiProjectileServices.find().orElse(null);
        PiLevelServiceStorage previousLevelStorage = PiLevelServiceStorages.find().orElse(null);
        PiEntityLifecycleService customEntityLifecycle = new PiEntityLifecycleService() {
            @Override
            public <E extends Entity> void register(Class<E> entityType, org.pickaid.pibrary.api.entity.PiEntityLifecycleHandler<? super E> handler) {
            }

            @Override
            public void onJoinLevel(Entity entity, Level level, boolean loadedFromDisk) {
            }

            @Override
            public void onEnterSection(Entity entity, net.minecraft.core.SectionPos oldSection, net.minecraft.core.SectionPos newSection) {
            }
        };
        PiVehicleLifecycleService customVehicleLifecycle = new PiVehicleLifecycleService() {
            @Override
            public <E extends Entity> void register(Class<E> entityType, org.pickaid.pibrary.api.entity.PiVehicleLifecycleHandler<? super E> handler) {
            }

            @Override
            public void onMount(Entity entity, Entity vehicle, Level level) {
            }

            @Override
            public void onDismount(Entity entity, Entity vehicle, Level level) {
            }
        };
        PiEntitySpatialIndex customSpatial = new PiEntitySpatialIndex() {
            @Override
            public List<Entity> query(Level level, AABB bounds, Predicate<Entity> filter) {
                return List.of();
            }
        };
        PiTargetingService customTargeting = (caster, query) -> List.of();
        PiProjectileLifecycleService customProjectileLifecycle = new PiProjectileLifecycleService() {
            @Override
            public <P extends net.minecraft.world.entity.projectile.Projectile> void register(
                    Class<P> projectileType,
                    org.pickaid.pibrary.api.projectile.PiProjectileLifecycleHandler<? super P> handler
            ) {
            }

            @Override
            public void onJoinLevel(
                    net.minecraft.world.entity.projectile.Projectile projectile,
                    org.pickaid.pibrary.api.projectile.PiProjectileLifecycleContext context
            ) {
            }
        };
        PiProjectileImpactService customProjectileImpact = new PiProjectileImpactService() {
            @Override
            public <P extends net.minecraft.world.entity.projectile.Projectile> void register(
                    Class<P> projectileType,
                    org.pickaid.pibrary.api.projectile.PiProjectileImpactHandler<? super P> handler
            ) {
            }

            @Override
            public void onImpact(
                    net.minecraft.world.entity.projectile.Projectile projectile,
                    org.pickaid.pibrary.api.projectile.PiProjectileImpactContext context
            ) {
            }
        };
        PiProjectileService customProjectile = request -> null;
        PiLevelServiceStorage customLevelStorage = new PiLevelServiceStorage() {
            @Override
            public <T extends org.pickaid.pibrary.api.service.PiStateLevelService<?>> T resolve(
                    net.minecraft.server.level.ServerLevel level,
                    org.pickaid.pibrary.api.service.PiLevelServiceDescriptor<T, ?> descriptor
            ) {
                return descriptor.create(new org.pickaid.pibrary.api.service.PiLevelServiceContext(
                        level,
                        org.pickaid.pibrary.api.core.PibraryServices.create()
                ));
            }
        };
        try {
            PiEntityLifecycleServices.install(customEntityLifecycle);
            PiVehicleLifecycleServices.install(customVehicleLifecycle);
            PiEntitySpatialServices.install(customSpatial);
            PiTargetingServices.install(customTargeting);
            PiProjectileLifecycleServices.install(customProjectileLifecycle);
            PiProjectileImpactServices.install(customProjectileImpact);
            PiProjectileServices.install(customProjectile);
            PiLevelServiceStorages.install(customLevelStorage);

            PibraryRuntimeServices.bootstrap();

            assertSame(customEntityLifecycle, PiEntityLifecycleServices.require());
            assertSame(customVehicleLifecycle, PiVehicleLifecycleServices.require());
            assertSame(customSpatial, PiEntitySpatialServices.require());
            assertSame(customTargeting, PiTargetingServices.require());
            assertSame(customProjectileLifecycle, PiProjectileLifecycleServices.require());
            assertSame(customProjectileImpact, PiProjectileImpactServices.require());
            assertSame(customProjectile, PiProjectileServices.require());
            assertSame(customLevelStorage, PiLevelServiceStorages.require());
        } finally {
            PiEntityLifecycleServices.install(previousEntityLifecycle);
            PiVehicleLifecycleServices.install(previousVehicleLifecycle);
            PiEntitySpatialServices.install(previousSpatial);
            PiTargetingServices.install(previousTargeting);
            PiProjectileLifecycleServices.install(previousProjectileLifecycle);
            PiProjectileImpactServices.install(previousProjectileImpact);
            PiProjectileServices.install(previousProjectile);
            PiLevelServiceStorages.install(previousLevelStorage);
        }
    }
}
