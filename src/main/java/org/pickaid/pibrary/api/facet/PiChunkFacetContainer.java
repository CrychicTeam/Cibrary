package org.pickaid.pibrary.api.facet;

import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryScope;

/**
 * Container abstraction used to resolve peer chunk facets and expose a shared scope.
 */
public interface PiChunkFacetContainer {
    /**
     * Returns the owning chunk when available.
     *
     * @return owning chunk or {@code null}
     */
    @Nullable
    LevelChunk chunk();

    /**
     * Returns the container-level shared scope.
     *
     * @return shared scope
     */
    PibraryScope scope();

    /**
     * Resolves an attached chunk facet from this container.
     *
     * @param facetClass requested facet class
     * @param <T> facet type
     * @return resolved facet
     */
    <T extends PiStateChunkFacet<?>> T get(Class<T> facetClass);
}
