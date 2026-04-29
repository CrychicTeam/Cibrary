package org.pickaid.pibrary.api.facet;

import java.util.Objects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryScope;

/**
 * Immutable construction context passed to level facets.
 */
public final class PiLevelFacetContext {
    private final @Nullable ServerLevel level;
    private final PibraryScope sharedScope;
    private final PibraryScope scope;

    /**
     * Creates a context with a fresh child scope derived from the shared level scope.
     *
     * @param level owning level, when available
     * @param sharedScope shared level scope
     */
    public PiLevelFacetContext(@Nullable ServerLevel level, PibraryScope sharedScope) {
        this(level, sharedScope, sharedScope.child());
    }

    /**
     * Creates a context with an explicit local scope.
     *
     * @param level owning level, when available
     * @param sharedScope shared level scope
     * @param scope local scope for the facet instance
     */
    public PiLevelFacetContext(@Nullable ServerLevel level, PibraryScope sharedScope, PibraryScope scope) {
        this.level = level;
        this.sharedScope = Objects.requireNonNull(sharedScope, "sharedScope");
        this.scope = Objects.requireNonNull(scope, "scope");
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
    public @Nullable MinecraftServer server() {
        return level == null ? null : level.getServer();
    }

    /**
     * Returns the scope owned by this facet instance.
     *
     * @return local scope
     */
    public PibraryScope scope() {
        return scope;
    }

    /**
     * Returns the shared level scope.
     *
     * @return shared level scope
     */
    public PibraryScope sharedScope() {
        return sharedScope;
    }
}
