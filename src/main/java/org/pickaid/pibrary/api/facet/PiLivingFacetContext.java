package org.pickaid.pibrary.api.facet;

import java.util.Objects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryServiceContext;

/**
 * Immutable construction context passed to living facets.
 */
public final class PiLivingFacetContext {
    private final @Nullable LivingEntity living;
    private final PiLivingFacetContainer container;
    private final PibraryServiceContext services;

    /**
     * Creates a context with a fresh child service scope derived from the container.
     *
     * @param living owning entity, when available
     * @param container owning facet container
     */
    public PiLivingFacetContext(@Nullable LivingEntity living, PiLivingFacetContainer container) {
        this(living, container, container.services().child());
    }

    /**
     * Creates a context with an explicit scoped service registry.
     *
     * @param living owning entity, when available
     * @param container owning facet container
     * @param services scoped service registry for the facet instance
     */
    public PiLivingFacetContext(@Nullable LivingEntity living, PiLivingFacetContainer container, PibraryServiceContext services) {
        this.living = living;
        this.container = Objects.requireNonNull(container, "container");
        this.services = Objects.requireNonNull(services, "services");
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
     * Returns the scoped service registry local to this facet instance.
     *
     * @return local service registry
     */
    public PibraryServiceContext services() {
        return services;
    }

    /**
     * Returns the shared container-level service registry.
     *
     * @return shared container registry
     */
    public PibraryServiceContext sharedServices() {
        return container.services();
    }
}
