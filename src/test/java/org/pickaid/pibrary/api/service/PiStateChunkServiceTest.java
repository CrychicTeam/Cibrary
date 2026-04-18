package org.pickaid.pibrary.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.junit.jupiter.api.Test;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.pibrary.api.core.PibraryServices;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import sun.misc.Unsafe;

class PiStateChunkServiceTest {
    @Test
    void persistentDataUsesPersistedProjection() {
        TestChunkService source = newService();
        source.increment();
        source.gainEnergy(3);
        source.setSessionGlow(14);

        CompoundTag persisted = source.savePersistentData();

        assertTrue(persisted.contains("count"));
        assertTrue(persisted.contains("energy"));
        assertFalse(persisted.contains("session_glow"));

        TestChunkService restored = newService();
        restored.setSessionGlow(99);
        restored.loadPersistentData(persisted, PiDecodeContext.strict());

        assertEquals(1, restored.count());
        assertEquals(3, restored.energy());
        assertEquals(99, restored.sessionGlow());
    }

    @Test
    void syncPayloadRespectsRouteVisibility() {
        TestChunkService service = newService();
        service.increment();
        service.gainEnergy(2);

        CompoundTag ownerPayload = service.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);
        CompoundTag trackingPayload = service.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.TRACKING);

        assertTrue(ownerPayload.contains("count"));
        assertTrue(ownerPayload.contains("energy"));
        assertTrue(trackingPayload.contains("count"));
        assertFalse(trackingPayload.contains("energy"));
    }

    @Test
    void detachedContextDoesNotExposeFakeChunkPosition() {
        PiChunkServiceContext context = new PiChunkServiceContext(null, null, PibraryServices.create());

        IllegalStateException exception = assertThrows(IllegalStateException.class, context::chunkPos);

        assertTrue(exception.getMessage().contains("Detached"));
    }

    @Test
    void rejectsMixedNullContext() {
        LevelChunk chunk = allocate(LevelChunk.class);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new PiChunkServiceContext(chunk, null, PibraryServices.create()));

        assertTrue(exception.getMessage().contains("both be present or both be null"));
    }

    @Test
    void deferredChunkApiContractsAreExplicit() {
        LevelChunk chunk = allocate(LevelChunk.class);
        ChunkPos pos = new ChunkPos(5, 6);

        UnsupportedOperationException register = assertThrows(
                UnsupportedOperationException.class,
                () -> PiChunkServices.host(TestChunkService.class).register()
        );
        assertTrue(register.getMessage().contains("Task 3"));

        UnsupportedOperationException type = assertThrows(
                UnsupportedOperationException.class,
                () -> PiChunkServices.type(TestChunkService.class)
        );
        assertTrue(type.getMessage().contains("Task 3"));

        UnsupportedOperationException findChunk = assertThrows(
                UnsupportedOperationException.class,
                () -> PiChunkServices.find(chunk, TestChunkService.class)
        );
        assertTrue(findChunk.getMessage().contains("Task 3"));

        UnsupportedOperationException findPos = assertThrows(
                UnsupportedOperationException.class,
                () -> PiChunkServices.find((ServerLevel) null, pos, TestChunkService.class)
        );
        assertTrue(findPos.getMessage().contains("Task 3"));

        UnsupportedOperationException require = assertThrows(
                UnsupportedOperationException.class,
                () -> PiChunkServices.require(chunk, TestChunkService.class)
        );
        assertTrue(require.getMessage().contains("Task 3"));

        UnsupportedOperationException resolve = assertThrows(
                UnsupportedOperationException.class,
                () -> PiChunkServices.resolve((ServerLevel) null, pos, TestChunkService.class)
        );
        assertTrue(resolve.getMessage().contains("Task 3"));

        PiChunkServiceType<TestChunkService> typeHandle = new PiChunkServiceType<>() {
            @Override
            public ResourceLocation id() {
                return ResourceLocation.fromNamespaceAndPath("pibrary", "test_chunk");
            }

            @Override
            public Class<TestChunkService> serviceType() {
                return TestChunkService.class;
            }

            @Override
            public boolean isRegistered() {
                return false;
            }
        };

        UnsupportedOperationException directFind = assertThrows(
                UnsupportedOperationException.class,
                () -> typeHandle.find(chunk)
        );
        assertTrue(directFind.getMessage().contains("Task 3"));
    }

    private static final class TestChunkService extends PiStateChunkService<CounterState> {
        private TestChunkService(PiChunkServiceContext context) {
            super(context);
        }

        private void increment() {
            updateState(state -> state.count++);
        }

        private void gainEnergy(int value) {
            updateState(state -> state.energy += value);
        }

        private void setSessionGlow(int value) {
            updateState(state -> state.sessionGlow = value);
        }

        private int count() {
            return viewState().count;
        }

        private int energy() {
            return viewState().energy;
        }

        private int sessionGlow() {
            return viewState().sessionGlow;
        }
    }

    private static TestChunkService newService() {
        return new TestChunkService(new PiChunkServiceContext(null, null, PibraryServices.create()));
    }

    private static Unsafe unsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static <T> T allocate(Class<T> type) {
        try {
            return type.cast(unsafe().allocateInstance(type));
        } catch (InstantiationException exception) {
            throw new AssertionError(exception);
        }
    }
}
