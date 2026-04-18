package org.pickaid.pibrary.api.service;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.pickaid.pibrary.runtime.chunk.PiActiveChunkServiceRegistry;
import org.pickaid.pibrary.runtime.chunk.PiChunkRuntimeAccess;

/**
 * Helpers for chunk-scoped services.
 */
public final class PiChunkServices {
    private PiChunkServices() {
    }

    public static <T extends PiStateChunkService<?>> PiChunkServiceRegistrar<T> host(Class<T> serviceType) {
        return new PiChunkServiceRegistrar<>(serviceType);
    }

    public static <T extends PiStateChunkService<?>> PiChunkServiceType<T> type(Class<T> serviceType) {
        return PiActiveChunkServiceRegistry.require(serviceType);
    }

    public static <T extends PiStateChunkService<?>> Optional<T> find(LevelChunk chunk, Class<T> serviceType) {
        return chunk == null
                ? Optional.empty()
                : PiActiveChunkServiceRegistry.find(serviceType).flatMap(ignored -> PiChunkRuntimeAccess.find(chunk, serviceType));
    }

    public static <T extends PiStateChunkService<?>> Optional<T> find(
            ServerLevel level,
            ChunkPos chunkPos,
            Class<T> serviceType
    ) {
        return chunkPos == null
                ? Optional.empty()
                : PiActiveChunkServiceRegistry.find(serviceType).flatMap(ignored -> PiChunkRuntimeAccess.find(level, chunkPos, serviceType));
    }

    public static <T extends PiStateChunkService<?>> T require(LevelChunk chunk, Class<T> serviceType) {
        return type(serviceType).get(chunk);
    }

    public static <T extends PiStateChunkService<?>> T resolve(ServerLevel level, ChunkPos chunkPos, Class<T> serviceType) {
        PiActiveChunkServiceRegistry.require(serviceType);
        return PiChunkRuntimeAccess.resolve(level, chunkPos, serviceType);
    }
}
