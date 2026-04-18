package org.pickaid.pibrary.runtime.chunk;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.pickaid.pibrary.api.service.PiStateChunkService;

public final class PiChunkRuntimeAccess {
    private static final Access DEFAULT = new DefaultAccess();
    private static volatile Access access = DEFAULT;

    private PiChunkRuntimeAccess() {
    }

    public static void install(Access access) {
        PiChunkRuntimeAccess.access = Objects.requireNonNull(access, "access");
    }

    public static void reset() {
        access = DEFAULT;
    }

    public static <T extends PiStateChunkService<?>> Optional<T> find(LevelChunk chunk, Class<T> serviceType) {
        return chunk == null ? Optional.empty() : access.find(chunk, serviceType);
    }

    public static <T extends PiStateChunkService<?>> Optional<T> find(
            ServerLevel level,
            ChunkPos chunkPos,
            Class<T> serviceType
    ) {
        if (chunkPos == null) {
            return Optional.empty();
        }
        LevelChunk chunk = access.getLoadedChunk(level, chunkPos);
        return chunk == null ? Optional.empty() : access.find(chunk, serviceType);
    }

    public static <T extends PiStateChunkService<?>> T resolve(ServerLevel level, ChunkPos chunkPos, Class<T> serviceType) {
        LevelChunk chunk = access.getOrCreateChunk(
                level,
                Objects.requireNonNull(chunkPos, "chunkPos"));
        return access.find(chunk, serviceType).orElseThrow(() ->
                new IllegalStateException("Missing Pi chunk service " + serviceType.getName() + " on " + chunkPos));
    }

    public interface Access {
        LevelChunk getLoadedChunk(ServerLevel level, ChunkPos chunkPos);

        LevelChunk getOrCreateChunk(ServerLevel level, ChunkPos chunkPos);

        <T extends PiStateChunkService<?>> Optional<T> find(LevelChunk chunk, Class<T> serviceType);
    }

    private static final class DefaultAccess implements Access {
        @Override
        public LevelChunk getLoadedChunk(ServerLevel level, ChunkPos chunkPos) {
            return level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        }

        @Override
        public LevelChunk getOrCreateChunk(ServerLevel level, ChunkPos chunkPos) {
            return level.getChunk(chunkPos.x, chunkPos.z);
        }

        @Override
        public <T extends PiStateChunkService<?>> Optional<T> find(LevelChunk chunk, Class<T> serviceType) {
            return PiActiveChunkServiceRegistry.find(Objects.requireNonNull(serviceType, "serviceType"))
                    .flatMap(type -> type.find(Objects.requireNonNull(chunk, "chunk")));
        }
    }
}
