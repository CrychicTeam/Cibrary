package org.pickaid.pibrary.api.service;

import java.util.Objects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryServiceContext;

/**
 * Immutable construction context passed to chunk services.
 */
public final class PiChunkServiceContext {
    private final @Nullable LevelChunk chunk;
    private final @Nullable ServerLevel level;
    private final @Nullable ChunkPos chunkPos;
    private final PibraryServiceContext sharedServices;
    private final PibraryServiceContext services;

    /**
     * Creates a context with a fresh child service scope derived from the shared chunk scope.
     *
     * @param chunk owning chunk, when available
     * @param level owning level, when available
     * @param sharedServices shared chunk service registry
     */
    public PiChunkServiceContext(@Nullable LevelChunk chunk, @Nullable ServerLevel level, PibraryServiceContext sharedServices) {
        this(chunk, level, sharedServices, sharedServices.child());
    }

    /**
     * Creates a context with an explicit scoped service registry.
     *
     * @param chunk owning chunk, when available
     * @param level owning level, when available
     * @param sharedServices shared chunk service registry
     * @param services scoped registry for the service instance
     */
    public PiChunkServiceContext(
            @Nullable LevelChunk chunk,
            @Nullable ServerLevel level,
            PibraryServiceContext sharedServices,
            PibraryServiceContext services
    ) {
        if (chunk == null ^ level == null) {
            throw new IllegalArgumentException("chunk and level must both be present or both be null");
        }
        if (chunk != null && chunk.getLevel() != level) {
            throw new IllegalArgumentException("chunk must belong to the provided level");
        }
        this.chunk = chunk;
        this.level = level;
        this.chunkPos = chunk == null ? null : chunk.getPos();
        this.sharedServices = Objects.requireNonNull(sharedServices, "sharedServices");
        this.services = Objects.requireNonNull(services, "services");
    }

    /**
     * Returns the owning chunk when available.
     *
     * @return owning chunk or {@code null}
     */
    public @Nullable LevelChunk chunk() {
        return chunk;
    }

    /**
     * Returns the owning server level when available.
     *
     * @return owning level or {@code null}
     */
    public @Nullable ServerLevel level() {
        return level;
    }

    /**
     * Returns the owning server when the level is present.
     *
     * @return owning server or {@code null}
     */
    public @Nullable net.minecraft.server.MinecraftServer server() {
        return level == null ? null : level.getServer();
    }

    /**
     * Returns the chunk position used by this context.
     *
     * @return chunk position
     */
    public ChunkPos chunkPos() {
        if (chunkPos == null) {
            throw new IllegalStateException("Detached chunk service context has no chunk position");
        }
        return chunkPos;
    }

    /**
     * Returns the scoped service registry owned by this service instance.
     *
     * @return local service registry
     */
    public PibraryServiceContext services() {
        return services;
    }

    /**
     * Returns the shared chunk-level service registry.
     *
     * @return shared chunk registry
     */
    public PibraryServiceContext sharedServices() {
        return sharedServices;
    }
}
