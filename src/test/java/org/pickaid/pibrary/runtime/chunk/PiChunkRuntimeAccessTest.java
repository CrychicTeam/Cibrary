package org.pickaid.pibrary.runtime.chunk;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.service.PiChunkServiceContext;
import org.pickaid.pibrary.api.service.PiChunkServices;
import org.pickaid.pibrary.api.service.PiStateChunkService;
import org.pickaid.pibrary.api.core.PibraryServices;
import org.pickaid.pibrary.dev.example.CounterChunkService;
import sun.misc.Unsafe;

class PiChunkRuntimeAccessTest {
    @AfterEach
    void resetAccess() {
        PiChunkRuntimeAccess.reset();
        PiActiveChunkServiceRegistry.clearForTests();
    }

    @Test
    void findUsesLoadedLookupWithoutResolvingChunks() {
        ServerLevel level = null;
        ChunkPos pos = new ChunkPos(5, 6);
        CounterChunkService service = new CounterChunkService(new PiChunkServiceContext(null, null, PibraryServices.create()));
        FakeChunkRuntimeAccess access = new FakeChunkRuntimeAccess(service);
        PiChunkRuntimeAccess.install(access);
        PiChunkServices.host(CounterChunkService.class).register();

        Optional<CounterChunkService> found = PiChunkRuntimeAccess.find(level, pos, CounterChunkService.class);

        assertTrue(found.isPresent());
        assertSame(service, found.orElseThrow());
        assertSame(level, access.loadedLookupLevel);
        assertSame(pos, access.loadedLookupPos);
        assertTrue(access.loadedLookupCalls == 1);
        assertTrue(access.resolveCalls == 0);
    }

    @Test
    void resolveUsesExplicitResolvingPath() {
        ServerLevel level = null;
        ChunkPos pos = new ChunkPos(7, 8);
        CounterChunkService service = new CounterChunkService(new PiChunkServiceContext(null, null, PibraryServices.create()));
        FakeChunkRuntimeAccess access = new FakeChunkRuntimeAccess(service);
        PiChunkRuntimeAccess.install(access);
        PiChunkServices.host(CounterChunkService.class).register();

        CounterChunkService resolved = PiChunkRuntimeAccess.resolve(level, pos, CounterChunkService.class);

        assertSame(service, resolved);
        assertSame(level, access.resolveLevel);
        assertSame(pos, access.resolvePos);
        assertTrue(access.loadedLookupCalls == 0);
        assertTrue(access.resolveCalls == 1);
    }

    private static final class FakeChunkRuntimeAccess implements PiChunkRuntimeAccess.Access {
        private final CounterChunkService service;
        private ServerLevel loadedLookupLevel;
        private ChunkPos loadedLookupPos;
        private ServerLevel resolveLevel;
        private ChunkPos resolvePos;
        private int loadedLookupCalls;
        private int resolveCalls;

        private FakeChunkRuntimeAccess(CounterChunkService service) {
            this.service = service;
        }

        @Override
        public LevelChunk getLoadedChunk(ServerLevel level, ChunkPos chunkPos) {
            loadedLookupCalls++;
            loadedLookupLevel = level;
            loadedLookupPos = chunkPos;
            return allocate(LevelChunk.class);
        }

        @Override
        public LevelChunk getOrCreateChunk(ServerLevel level, ChunkPos chunkPos) {
            resolveCalls++;
            resolveLevel = level;
            resolvePos = chunkPos;
            return allocate(LevelChunk.class);
        }

        @Override
        public <T extends PiStateChunkService<?>> Optional<T> find(LevelChunk chunk, Class<T> serviceType) {
            return Optional.of(serviceType.cast(service));
        }
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
