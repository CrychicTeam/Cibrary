package org.pickaid.pibrary.runtime.facet;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryScope;
import org.pickaid.pibrary.api.core.PibraryScopes;
import org.pickaid.pibrary.api.facet.PiChunkFacetContainer;
import org.pickaid.pibrary.api.facet.PiStateChunkFacet;

public final class PiAttachedChunkFacetContainer implements PiChunkFacetContainer {
    private final @Nullable LevelChunk chunk;
    private final PibraryScope scope;
    private final Map<Class<?>, PiStateChunkFacet<?>> attached = new ConcurrentHashMap<>();

    public PiAttachedChunkFacetContainer(@Nullable LevelChunk chunk) {
        this(chunk, PibraryScopes.create());
    }

    public PiAttachedChunkFacetContainer(@Nullable LevelChunk chunk, PibraryScope scope) {
        this.chunk = chunk;
        this.scope = Objects.requireNonNull(scope, "scope");
    }

    @Override
    public @Nullable LevelChunk chunk() {
        return chunk;
    }

    @Override
    public PibraryScope scope() {
        return scope;
    }

    public <T extends PiStateChunkFacet<?>> void attach(Class<T> facetClass, T facet) {
        attached.put(Objects.requireNonNull(facetClass, "facetClass"), Objects.requireNonNull(facet, "facet"));
    }

    @Override
    public <T extends PiStateChunkFacet<?>> T get(Class<T> facetClass) {
        PiStateChunkFacet<?> existing = attached.get(facetClass);
        if (existing != null) {
            return facetClass.cast(existing);
        }
        if (chunk == null) {
            throw new IllegalStateException("Cannot resolve Pi chunk facet " + facetClass.getName() + " without a chunk");
        }
        return PiChunkFacetDescriptors.requireGenerated(facetClass).get(chunk);
    }
}
