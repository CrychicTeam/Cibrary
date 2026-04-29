package org.pickaid.pibrary.api.facet;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;

public interface PiChunkFacetType<T extends PiStateChunkFacet<?>> {
    ResourceLocation id();

    Class<T> facetClass();

    Capability<T> capability();

    boolean isRegistered();

    Optional<T> find(LevelChunk chunk);

    default T get(LevelChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        return find(chunk).orElseThrow(() ->
                new IllegalStateException("Missing Pi chunk facet " + facetClass().getName() + " on " + chunk.getPos()));
    }

    default boolean supports(LevelChunk chunk) {
        return chunk.getCapability(capability()).isPresent();
    }
}
