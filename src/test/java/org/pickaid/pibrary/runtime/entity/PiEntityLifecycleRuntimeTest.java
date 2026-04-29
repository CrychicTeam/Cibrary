package org.pickaid.pibrary.runtime.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.entity.PiEntityLifecycleHandler;
import org.pickaid.pibrary.api.entity.PiEntityLifecycleRegistry;
import org.pickaid.pibrary.api.entity.PiEntityLifecycles;
import org.pickaid.pibrary.runtime.core.PibraryRuntimeBootstrap;

class PiEntityLifecycleRuntimeTest {
    @Test
    void bootstrapInstallsDefaultEntityLifecycleRegistryWhenMissing() {
        PiEntityLifecycleRegistry previous = PiEntityLifecycles.find().orElse(null);
        try {
            PiEntityLifecycles.install(null);

            PibraryRuntimeBootstrap.bootstrap();

            assertTrue(PiEntityLifecycles.require() instanceof PiDefaultEntityLifecycleRegistry);
        } finally {
            PiEntityLifecycles.install(previous);
        }
    }

    @Test
    void lifecycleRegistryResolvesHandlersByEntityType() {
        PiDefaultEntityLifecycleRegistry registry = new PiDefaultEntityLifecycleRegistry();
        JoinSectionHandler allEntities = new JoinSectionHandler();
        JoinSectionHandler exactEntities = new JoinSectionHandler();
        JoinSectionHandler onlyOtherEntities = new JoinSectionHandler();

        registry.register(Entity.class, allEntities);
        registry.register(TestEntity.class, exactEntities);
        registry.register(OtherEntity.class, onlyOtherEntities);

        assertEquals(2, registry.handlersFor(TestEntity.class).size());
        assertTrue(registry.handlersFor(TestEntity.class).contains(allEntities));
        assertTrue(registry.handlersFor(TestEntity.class).contains(exactEntities));
        assertEquals(1, registry.handlersFor(OtherEntity.class).stream().filter(allEntities::equals).count());
        assertEquals(1, registry.handlersFor(OtherEntity.class).stream().filter(onlyOtherEntities::equals).count());
    }

    @Test
    void lifecycleRegistryInvalidatesResolvedTypeCacheWhenNewHandlerIsRegistered() {
        PiDefaultEntityLifecycleRegistry registry = new PiDefaultEntityLifecycleRegistry();
        JoinSectionHandler allEntities = new JoinSectionHandler();
        JoinSectionHandler exactEntities = new JoinSectionHandler();

        registry.register(Entity.class, allEntities);
        assertEquals(1, registry.handlersFor(TestEntity.class).size());

        registry.register(TestEntity.class, exactEntities);

        assertEquals(2, registry.handlersFor(TestEntity.class).size());
        assertTrue(registry.handlersFor(TestEntity.class).contains(allEntities));
        assertTrue(registry.handlersFor(TestEntity.class).contains(exactEntities));
    }

    private static final class JoinSectionHandler implements PiEntityLifecycleHandler<Entity> {
    }

    private static final class TestEntity extends Entity {
        private TestEntity(net.minecraft.world.entity.EntityType<?> entityType, Level level) {
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

    private static final class OtherEntity extends Entity {
        private OtherEntity(net.minecraft.world.entity.EntityType<?> entityType, Level level) {
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
