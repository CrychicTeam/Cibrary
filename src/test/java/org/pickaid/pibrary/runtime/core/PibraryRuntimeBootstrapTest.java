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
import org.pickaid.pibrary.api.entity.PiEntityLifecycleRegistry;
import org.pickaid.pibrary.api.entity.PiEntityLifecycles;
import org.pickaid.pibrary.api.entity.PiEntitySpatialIndex;
import org.pickaid.pibrary.api.entity.PiEntitySpatialIndexes;
import org.pickaid.pibrary.api.entity.PiVehicleLifecycleRegistry;
import org.pickaid.pibrary.api.entity.PiVehicleLifecycles;
import org.pickaid.pibrary.api.facet.PiLevelFacetStorage;
import org.pickaid.pibrary.api.facet.PiLevelFacetStorages;
import org.pickaid.pibrary.api.projectile.PiProjectileImpactRegistry;
import org.pickaid.pibrary.api.projectile.PiProjectileImpacts;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycleRegistry;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycles;
import org.pickaid.pibrary.api.projectile.PiProjectileTracer;
import org.pickaid.pibrary.api.projectile.PiProjectileTraces;
import org.pickaid.pibrary.api.targeting.PiTargetingResolver;
import org.pickaid.pibrary.api.targeting.PiTargeting;
import org.pickaid.pibrary.runtime.entity.PiDefaultEntityLifecycleRegistry;
import org.pickaid.pibrary.runtime.entity.PiDefaultVehicleLifecycleRegistry;
import org.pickaid.pibrary.runtime.entity.PiSectionEntitySpatialIndex;
import org.pickaid.pibrary.runtime.facet.PiSavedDataLevelFacetStorage;
import org.pickaid.pibrary.runtime.projectile.PiDefaultProjectileImpactRegistry;
import org.pickaid.pibrary.runtime.projectile.PiDefaultProjectileLifecycleRegistry;
import org.pickaid.pibrary.runtime.projectile.PiDefaultProjectileTracer;
import org.pickaid.pibrary.runtime.targeting.PiDefaultTargetingResolver;

class PibraryRuntimeBootstrapTest {
    @Test
    void pibraryConstructorBootstrapsDefaultLevelFacetStorage() {
        PiLevelFacetStorage previousLevelStorage = PiLevelFacetStorages.find().orElse(null);
        try {
            PiLevelFacetStorages.install(null);

            new Pibrary();

            assertTrue(PiLevelFacetStorages.require() instanceof PiSavedDataLevelFacetStorage);
        } finally {
            PiLevelFacetStorages.install(previousLevelStorage);
        }
    }

    @Test
    void bootstrapInstallsDefaultRuntimeSystemsWhenMissing() {
        PiEntityLifecycleRegistry previousEntityLifecycle = PiEntityLifecycles.find().orElse(null);
        PiVehicleLifecycleRegistry previousVehicleLifecycle = PiVehicleLifecycles.find().orElse(null);
        PiEntitySpatialIndex previousSpatial = PiEntitySpatialIndexes.find().orElse(null);
        PiTargetingResolver previousTargeting = PiTargeting.find().orElse(null);
        PiProjectileLifecycleRegistry previousProjectileLifecycle = PiProjectileLifecycles.find().orElse(null);
        PiProjectileImpactRegistry previousProjectileImpact = PiProjectileImpacts.find().orElse(null);
        PiProjectileTracer previousProjectile = PiProjectileTraces.find().orElse(null);
        PiLevelFacetStorage previousLevelStorage = PiLevelFacetStorages.find().orElse(null);
        try {
            PiEntityLifecycles.install(null);
            PiVehicleLifecycles.install(null);
            PiEntitySpatialIndexes.install(null);
            PiTargeting.install(null);
            PiProjectileLifecycles.install(null);
            PiProjectileImpacts.install(null);
            PiProjectileTraces.install(null);
            PiLevelFacetStorages.install(null);

            PibraryRuntimeBootstrap.bootstrap();

            assertTrue(PiEntityLifecycles.require() instanceof PiDefaultEntityLifecycleRegistry);
            assertTrue(PiVehicleLifecycles.require() instanceof PiDefaultVehicleLifecycleRegistry);
            assertTrue(PiEntitySpatialIndexes.require() instanceof PiSectionEntitySpatialIndex);
            assertTrue(PiTargeting.require() instanceof PiDefaultTargetingResolver);
            assertTrue(PiProjectileLifecycles.require() instanceof PiDefaultProjectileLifecycleRegistry);
            assertTrue(PiProjectileImpacts.require() instanceof PiDefaultProjectileImpactRegistry);
            assertTrue(PiProjectileTraces.require() instanceof PiDefaultProjectileTracer);
            assertTrue(PiLevelFacetStorages.require() instanceof PiSavedDataLevelFacetStorage);
        } finally {
            PiEntityLifecycles.install(previousEntityLifecycle);
            PiVehicleLifecycles.install(previousVehicleLifecycle);
            PiEntitySpatialIndexes.install(previousSpatial);
            PiTargeting.install(previousTargeting);
            PiProjectileLifecycles.install(previousProjectileLifecycle);
            PiProjectileImpacts.install(previousProjectileImpact);
            PiProjectileTraces.install(previousProjectile);
            PiLevelFacetStorages.install(previousLevelStorage);
        }
    }

    @Test
    void bootstrapKeepsCustomRuntimeOverrides() {
        PiEntityLifecycleRegistry previousEntityLifecycle = PiEntityLifecycles.find().orElse(null);
        PiVehicleLifecycleRegistry previousVehicleLifecycle = PiVehicleLifecycles.find().orElse(null);
        PiEntitySpatialIndex previousSpatial = PiEntitySpatialIndexes.find().orElse(null);
        PiTargetingResolver previousTargeting = PiTargeting.find().orElse(null);
        PiProjectileLifecycleRegistry previousProjectileLifecycle = PiProjectileLifecycles.find().orElse(null);
        PiProjectileImpactRegistry previousProjectileImpact = PiProjectileImpacts.find().orElse(null);
        PiProjectileTracer previousProjectile = PiProjectileTraces.find().orElse(null);
        PiLevelFacetStorage previousLevelStorage = PiLevelFacetStorages.find().orElse(null);
        PiEntityLifecycleRegistry customEntityLifecycle = new PiEntityLifecycleRegistry() {
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
        PiVehicleLifecycleRegistry customVehicleLifecycle = new PiVehicleLifecycleRegistry() {
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
        PiTargetingResolver customTargeting = (caster, query) -> List.of();
        PiProjectileLifecycleRegistry customProjectileLifecycle = new PiProjectileLifecycleRegistry() {
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
        PiProjectileImpactRegistry customProjectileImpact = new PiProjectileImpactRegistry() {
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
        PiProjectileTracer customProjectile = request -> null;
        PiLevelFacetStorage customLevelStorage = new PiLevelFacetStorage() {
            @Override
            public <T extends org.pickaid.pibrary.api.facet.PiStateLevelFacet<?>> T resolve(
                    net.minecraft.server.level.ServerLevel level,
                    org.pickaid.pibrary.api.facet.PiLevelFacetDescriptor<T, ?> descriptor
            ) {
                return descriptor.create(new org.pickaid.pibrary.api.facet.PiLevelFacetContext(
                        level,
                        org.pickaid.pibrary.api.core.PibraryScopes.create()
                ));
            }
        };
        try {
            PiEntityLifecycles.install(customEntityLifecycle);
            PiVehicleLifecycles.install(customVehicleLifecycle);
            PiEntitySpatialIndexes.install(customSpatial);
            PiTargeting.install(customTargeting);
            PiProjectileLifecycles.install(customProjectileLifecycle);
            PiProjectileImpacts.install(customProjectileImpact);
            PiProjectileTraces.install(customProjectile);
            PiLevelFacetStorages.install(customLevelStorage);

            PibraryRuntimeBootstrap.bootstrap();

            assertSame(customEntityLifecycle, PiEntityLifecycles.require());
            assertSame(customVehicleLifecycle, PiVehicleLifecycles.require());
            assertSame(customSpatial, PiEntitySpatialIndexes.require());
            assertSame(customTargeting, PiTargeting.require());
            assertSame(customProjectileLifecycle, PiProjectileLifecycles.require());
            assertSame(customProjectileImpact, PiProjectileImpacts.require());
            assertSame(customProjectile, PiProjectileTraces.require());
            assertSame(customLevelStorage, PiLevelFacetStorages.require());
        } finally {
            PiEntityLifecycles.install(previousEntityLifecycle);
            PiVehicleLifecycles.install(previousVehicleLifecycle);
            PiEntitySpatialIndexes.install(previousSpatial);
            PiTargeting.install(previousTargeting);
            PiProjectileLifecycles.install(previousProjectileLifecycle);
            PiProjectileImpacts.install(previousProjectileImpact);
            PiProjectileTraces.install(previousProjectile);
            PiLevelFacetStorages.install(previousLevelStorage);
        }
    }
}
