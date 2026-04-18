package org.pickaid.pibrary.api.service;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import org.pickaid.pibrary.runtime.chunk.PiChunkRuntimeAccess;

/**
 * Typed handle for a registered chunk service.
 *
 * @param <T> service type
 */
public interface PiChunkServiceType<T extends PiStateChunkService<?>> {
    ResourceLocation id();

    Class<T> serviceType();

    boolean isRegistered();

    default Optional<T> find(LevelChunk chunk) {
        return PiChunkRuntimeAccess.find(chunk, serviceType());
    }

    default T get(LevelChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        return find(chunk).orElseThrow(() ->
                new IllegalStateException("Missing Pi chunk service " + serviceType().getName() + " on " + chunk.getPos()));
    }
}
