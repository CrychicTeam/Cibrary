package org.pickaid.pibrary.api.facet;

import java.util.Objects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryScope;

/**
 * Immutable construction context passed to living facets.
 */
public final class PiLivingFacetContext {
    private final @Nullable LivingEntity living;
    private final PiLivingFacetContainer container;
    private final PibraryScope scope;

    /**
     * Creates a context with a fresh child scope derived from the container.
     *
     * @param living owning entity, when available
     * @param container owning facet container
     */
    public PiLivingFacetContext(@Nullable LivingEntity living, PiLivingFacetContainer container) {
        this(living, container, container.scope().child());
    }

    /**
     * Creates a context with an explicit local scope.
     *
     * @param living owning entity, when available
     * @param container owning facet container
     * @param scope local scope for the facet instance
     */
    public PiLivingFacetContext(@Nullable LivingEntity living, PiLivingFacetContainer container, PibraryScope scope) {
        this.living = living;
        this.container = Objects.requireNonNull(container, "container");
        this.scope = Objects.requireNonNull(scope, "scope");
    }

    /**
     * Returns the owning living entity when available.
     *
     * @return owning entity or {@code null}
     */
    public @Nullable LivingEntity living() {
        return living;
    }

    /**
     * Returns the owning player when the living entity is a player.
     *
     * @return owning player or {@code null}
     */
    public @Nullable Player player() {
        return living instanceof Player player ? player : null;
    }

    /**
     * Returns the container used to resolve peer facets.
     *
     * @return owning facet container
     */
    public PiLivingFacetContainer container() {
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
