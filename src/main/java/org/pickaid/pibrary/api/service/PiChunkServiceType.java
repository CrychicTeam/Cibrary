package org.pickaid.pibrary.api.service;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;

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
        throw new UnsupportedOperationException(
                "Chunk service lookup for " + serviceType().getName() + " is deferred until Task 3");
    }

    default T get(LevelChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        return find(chunk).orElseThrow(() ->
                new IllegalStateException("Missing Pi chunk service " + serviceType().getName() + " on " + chunk.getPos()));
    }
}
