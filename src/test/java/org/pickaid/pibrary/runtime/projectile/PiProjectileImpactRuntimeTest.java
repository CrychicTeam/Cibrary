package org.pickaid.pibrary.runtime.projectile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.projectile.PiProjectileImpactContext;
import org.pickaid.pibrary.api.projectile.PiProjectileImpactHandler;
import org.pickaid.pibrary.api.projectile.PiProjectileImpactResult;

class PiProjectileImpactRuntimeTest {
    @Test
    void projectileImpactRegistryResolvesHandlersByProjectileType() {
        PiDefaultProjectileImpactRegistry registry = new PiDefaultProjectileImpactRegistry();
        ImpactHandler allProjectiles = new ImpactHandler();
        ImpactHandler exactProjectiles = new ImpactHandler();
        ImpactHandler onlyOtherProjectiles = new ImpactHandler();

        registry.register(Projectile.class, allProjectiles);
        registry.register(TestProjectile.class, exactProjectiles);
        registry.register(OtherProjectile.class, onlyOtherProjectiles);

        assertEquals(2, registry.handlersFor(TestProjectile.class).size());
        assertTrue(registry.handlersFor(TestProjectile.class).contains(allProjectiles));
        assertTrue(registry.handlersFor(TestProjectile.class).contains(exactProjectiles));
        assertEquals(1, registry.handlersFor(OtherProjectile.class).stream().filter(allProjectiles::equals).count());
        assertEquals(1, registry.handlersFor(OtherProjectile.class).stream().filter(onlyOtherProjectiles::equals).count());
    }

    @Test
    void projectileImpactRegistryInvalidatesResolvedTypeCacheWhenNewHandlerIsRegistered() {
        PiDefaultProjectileImpactRegistry registry = new PiDefaultProjectileImpactRegistry();
        ImpactHandler allProjectiles = new ImpactHandler();
        ImpactHandler exactProjectiles = new ImpactHandler();

        registry.register(Projectile.class, allProjectiles);
        assertEquals(1, registry.handlersFor(TestProjectile.class).size());

        registry.register(TestProjectile.class, exactProjectiles);

        assertEquals(2, registry.handlersFor(TestProjectile.class).size());
        assertTrue(registry.handlersFor(TestProjectile.class).contains(allProjectiles));
        assertTrue(registry.handlersFor(TestProjectile.class).contains(exactProjectiles));
    }

    @Test
    void impactContextTracksCancellationAndResultChanges() {
        PiProjectileImpactContext context = new PiProjectileImpactContext(
                BlockHitResult.miss(net.minecraft.world.phys.Vec3.ZERO, Direction.UP, BlockPos.ZERO)
        );

        assertEquals(PiProjectileImpactResult.DEFAULT, context.result());
        assertFalse(context.isCanceled());

        context.setResult(PiProjectileImpactResult.SKIP_ENTITY);
        context.cancelImpact();

        assertEquals(PiProjectileImpactResult.SKIP_ENTITY, context.result());
        assertTrue(context.isCanceled());
    }

    @Test
    void projectileImpactEventsMapPiResultsToForgeResults() {
        assertEquals(ProjectileImpactEvent.ImpactResult.DEFAULT,
                PiProjectileImpactEvents.toForgeResult(PiProjectileImpactResult.DEFAULT));
        assertEquals(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY,
                PiProjectileImpactEvents.toForgeResult(PiProjectileImpactResult.SKIP_ENTITY));
        assertEquals(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT,
                PiProjectileImpactEvents.toForgeResult(PiProjectileImpactResult.STOP_AT_CURRENT));
        assertEquals(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE,
                PiProjectileImpactEvents.toForgeResult(PiProjectileImpactResult.STOP_AT_CURRENT_NO_DAMAGE));
    }

    private static final class ImpactHandler implements PiProjectileImpactHandler<Projectile> {
    }

    private static final class TestProjectile extends Projectile {
        private TestProjectile(net.minecraft.world.entity.EntityType<? extends Projectile> entityType, Level level) {
            super(entityType, level);
        }

        @Override
        protected void defineSynchedData() {
        }

        @Override
        protected void readAdditionalSaveData(CompoundTag tag) {
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag tag) {
        }

        @Override
        public Packet<ClientGamePacketListener> getAddEntityPacket() {
            return null;
        }
    }

    private static final class OtherProjectile extends Projectile {
        private OtherProjectile(net.minecraft.world.entity.EntityType<? extends Projectile> entityType, Level level) {
            super(entityType, level);
        }

        @Override
        protected void defineSynchedData() {
        }

        @Override
        protected void readAdditionalSaveData(CompoundTag tag) {
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag tag) {
        }

        @Override
        public Packet<ClientGamePacketListener> getAddEntityPacket() {
            return null;
        }
    }
}
