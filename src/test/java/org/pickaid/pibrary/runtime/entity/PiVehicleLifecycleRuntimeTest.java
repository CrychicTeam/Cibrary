package org.pickaid.pibrary.runtime.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.entity.PiVehicleLifecycleHandler;

class PiVehicleLifecycleRuntimeTest {
    @Test
    void vehicleLifecycleServiceResolvesHandlersByEntityType() {
        PiDefaultVehicleLifecycleService service = new PiDefaultVehicleLifecycleService();
        VehicleHandler allEntities = new VehicleHandler();
        VehicleHandler exactEntities = new VehicleHandler();
        VehicleHandler onlyOtherEntities = new VehicleHandler();

        service.register(Entity.class, allEntities);
        service.register(TestEntity.class, exactEntities);
        service.register(OtherEntity.class, onlyOtherEntities);

        assertEquals(2, service.handlersFor(TestEntity.class).size());
        assertTrue(service.handlersFor(TestEntity.class).contains(allEntities));
        assertTrue(service.handlersFor(TestEntity.class).contains(exactEntities));
        assertEquals(1, service.handlersFor(OtherEntity.class).stream().filter(allEntities::equals).count());
        assertEquals(1, service.handlersFor(OtherEntity.class).stream().filter(onlyOtherEntities::equals).count());
    }

    @Test
    void vehicleLifecycleServiceInvalidatesResolvedTypeCacheWhenNewHandlerIsRegistered() {
        PiDefaultVehicleLifecycleService service = new PiDefaultVehicleLifecycleService();
        VehicleHandler allEntities = new VehicleHandler();
        VehicleHandler exactEntities = new VehicleHandler();

        service.register(Entity.class, allEntities);
        assertEquals(1, service.handlersFor(TestEntity.class).size());

        service.register(TestEntity.class, exactEntities);

        assertEquals(2, service.handlersFor(TestEntity.class).size());
        assertTrue(service.handlersFor(TestEntity.class).contains(allEntities));
        assertTrue(service.handlersFor(TestEntity.class).contains(exactEntities));
    }

    private static final class VehicleHandler implements PiVehicleLifecycleHandler<Entity> {
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
