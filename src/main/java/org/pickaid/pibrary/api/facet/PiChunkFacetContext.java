package org.pickaid.pibrary.api.facet;

import java.util.Objects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryScope;

/**
 * Immutable construction context passed to chunk facets.
 */
public final class PiChunkFacetContext {
    private final @Nullable LevelChunk chunk;
    private final PiChunkFacetContainer container;
    private final PibraryScope scope;

    /**
     * Creates a context with a fresh child scope derived from the container.
     *
     * @param chunk owning chunk, when available
     * @param container owning facet container
     */
    public PiChunkFacetContext(@Nullable LevelChunk chunk, PiChunkFacetContainer container) {
        this(chunk, container, container.scope().child());
    }

    /**
     * Creates a context with an explicit local scope.
     *
     * @param chunk owning chunk, when available
     * @param container owning facet container
     * @param scope local scope for the facet instance
     */
    public PiChunkFacetContext(@Nullable LevelChunk chunk, PiChunkFacetContainer container, PibraryScope scope) {
        this.chunk = chunk;
        this.container = Objects.requireNonNull(container, "container");
        this.scope = Objects.requireNonNull(scope, "scope");
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
     * Returns the chunk's level when available.
     *
     * @return owning level or {@code null}
     */
    public @Nullable Level level() {
        return chunk == null ? null : chunk.getLevel();
    }

    /**
     * Returns the server level when this chunk belongs to one.
     *
     * @return owning server level or {@code null}
     */
    public @Nullable ServerLevel serverLevel() {
        return level() instanceof ServerLevel serverLevel ? serverLevel : null;
    }

    /**
     * Returns the container used to resolve peer facets.
     *
     * @return owning facet container
     */
    public PiChunkFacetContainer container() {
        return container;
    }

    /**
     * Returns the scope local to this facet instance.
     *
     * @return local scope
     */
    public PibraryScope scope() {
        return scope;
    }

    /**
     * Returns the shared container-level scope.
     *
     * @return shared container scope
     */
    public PibraryScope sharedScope() {
        return container.scope();
    }
}
