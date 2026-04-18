package org.pickaid.pibrary.api.service;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Helpers for chunk-scoped services.
 *
 * <p>Task 2 exposes the public API surface only. Runtime registration and chunk storage
 * are deferred to a later task.</p>
 */
public final class PiChunkServices {
    private PiChunkServices() {
    }

    public static <T extends PiStateChunkService<?>> PiChunkServiceRegistrar<T> host(Class<T> serviceType) {
        return new PiChunkServiceRegistrar<>(serviceType);
    }

    public static <T extends PiStateChunkService<?>> PiChunkServiceType<T> type(Class<T> serviceType) {
        throw deferred("type", serviceType);
    }

    public static <T extends PiStateChunkService<?>> Optional<T> find(LevelChunk chunk, Class<T> serviceType) {
        throw deferred("find(levelChunk)", serviceType);
    }

    public static <T extends PiStateChunkService<?>> Optional<T> find(
            ServerLevel level,
            ChunkPos chunkPos,
            Class<T> serviceType
    ) {
        throw deferred("find(level,chunkPos)", serviceType);
    }

    public static <T extends PiStateChunkService<?>> T require(LevelChunk chunk, Class<T> serviceType) {
        throw deferred("require(levelChunk)", serviceType);
    }

    public static <T extends PiStateChunkService<?>> T resolve(ServerLevel level, ChunkPos chunkPos, Class<T> serviceType) {
        throw deferred("resolve(level,chunkPos)", serviceType);
    }

    private static UnsupportedOperationException deferred(String operation, Class<?> serviceType) {
        return new UnsupportedOperationException(
                "Chunk service " + operation + " for " + serviceType.getName() + " is deferred until Task 3");
    }
}
