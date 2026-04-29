package org.pickaid.pibrary.api.facet;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.runtime.facet.PiActiveChunkFacetRegistry;

/**
 * Entry points for registering and resolving chunk-scoped facets.
 */
public final class PiChunkFacets {
    private PiChunkFacets() {
    }

    public static <T extends PiStateChunkFacet<?>> PiChunkFacetBinding<T> bind(Class<T> facetClass) {
        return new PiChunkFacetBinding<>(facetClass);
    }

    public static <T extends PiStateChunkFacet<?>> PiChunkFacetType<T> type(Class<T> facetClass) {
        return PiActiveChunkFacetRegistry.require(facetClass);
    }

    public static <T extends PiStateChunkFacet<?>> Optional<T> find(@Nullable LevelChunk chunk, Class<T> facetClass) {
        return chunk == null
                ? Optional.empty()
                : PiActiveChunkFacetRegistry.find(facetClass).flatMap(type -> type.find(chunk));
    }

    public static <T extends PiStateChunkFacet<?>> T require(LevelChunk chunk, Class<T> facetClass) {
        return type(facetClass).get(chunk);
    }

    /**
     * Finds a facet only if the chunk is already loaded.
     *
     * <p>This deliberately avoids loading or generating chunks as a side effect.</p>
     *
     * @param level server level
     * @param pos chunk position
     * @param facetClass requested facet type
     * @param <T> facet type
     * @return attached facet if the chunk is loaded and has the capability
     */
    public static <T extends PiStateChunkFacet<?>> Optional<T> findLoaded(
            @Nullable ServerLevel level,
            ChunkPos pos,
            Class<T> facetClass
    ) {
        if (level == null) {
            return Optional.empty();
        }
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
        return find(chunk, facetClass);
    }

    public static <T extends PiStateChunkFacet<?>> T requireLoaded(
            ServerLevel level,
            ChunkPos pos,
            Class<T> facetClass
    ) {
        return findLoaded(level, pos, facetClass).orElseThrow(() ->
                new IllegalStateException("Missing loaded Pi chunk facet " + facetClass.getName() + " at " + pos));
    }
}
