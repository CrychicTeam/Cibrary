package org.pickaid.pibrary.runtime.projectile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycleContext;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycleHandler;

class PiProjectileLifecycleRuntimeTest {
    @Test
    void projectileLifecycleServiceResolvesHandlersByProjectileType() {
        PiDefaultProjectileLifecycleService service = new PiDefaultProjectileLifecycleService();
        LifecycleHandler allProjectiles = new LifecycleHandler();
        LifecycleHandler exactProjectiles = new LifecycleHandler();
        LifecycleHandler onlyOtherProjectiles = new LifecycleHandler();

        service.register(Projectile.class, allProjectiles);
        service.register(TestProjectile.class, exactProjectiles);
        service.register(OtherProjectile.class, onlyOtherProjectiles);

        assertEquals(2, service.handlersFor(TestProjectile.class).size());
        assertTrue(service.handlersFor(TestProjectile.class).contains(allProjectiles));
        assertTrue(service.handlersFor(TestProjectile.class).contains(exactProjectiles));
        assertEquals(1, service.handlersFor(OtherProjectile.class).stream().filter(allProjectiles::equals).count());
        assertEquals(1, service.handlersFor(OtherProjectile.class).stream().filter(onlyOtherProjectiles::equals).count());
    }

    @Test
    void projectileLifecycleServiceInvalidatesResolvedTypeCacheWhenNewHandlerIsRegistered() {
        PiDefaultProjectileLifecycleService service = new PiDefaultProjectileLifecycleService();
        LifecycleHandler allProjectiles = new LifecycleHandler();
        LifecycleHandler exactProjectiles = new LifecycleHandler();

        service.register(Projectile.class, allProjectiles);
        assertEquals(1, service.handlersFor(TestProjectile.class).size());

        service.register(TestProjectile.class, exactProjectiles);

        assertEquals(2, service.handlersFor(TestProjectile.class).size());
        assertTrue(service.handlersFor(TestProjectile.class).contains(allProjectiles));
        assertTrue(service.handlersFor(TestProjectile.class).contains(exactProjectiles));
    }

    @Test
    void projectileLifecycleContextTracksLoadSource() {
        PiProjectileLifecycleContext liveContext = new PiProjectileLifecycleContext(false);
        PiProjectileLifecycleContext loadedContext = new PiProjectileLifecycleContext(true);

        assertFalse(liveContext.loadedFromDisk());
        assertTrue(loadedContext.loadedFromDisk());
    }

    private static final class LifecycleHandler implements PiProjectileLifecycleHandler<Projectile> {
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
