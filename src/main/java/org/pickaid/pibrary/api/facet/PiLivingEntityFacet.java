package org.pickaid.pibrary.api.facet;

import java.util.Objects;
import java.util.Optional;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServiceKey;

/**
 * Base class for living-entity-scoped facets.
 */
public abstract class PiLivingEntityFacet {
    private final PiLivingFacetContext context;

    protected PiLivingEntityFacet(PiLivingFacetContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    /**
     * Returns the immutable facet context.
     *
     * @return facet context
     */
    public final PiLivingFacetContext context() {
        return context;
    }

    /**
     * Returns the scoped service registry owned by this facet instance.
     *
     * @return scoped service registry
     */
    protected final PibraryServiceContext services() {
        return context.services();
    }

    /**
     * Returns the shared container-level service registry.
     *
     * @return shared container service registry
     */
    protected final PibraryServiceContext sharedServices() {
        return context.sharedServices();
    }

    /**
     * Finds another service in the local scoped registry.
     *
     * @param key service key
     * @param <T> service type
     * @return resolved service, if present
     */
    protected final <T> Optional<T> findService(PibraryServiceKey<T> key) {
        return services().find(key);
    }

    /**
     * Requires another service from the local scoped registry.
     *
     * @param key service key
     * @param <T> service type
     * @return resolved service
     */
    protected final <T> T requireService(PibraryServiceKey<T> key) {
        return services().require(key);
    }

    /**
     * Resolves another attached living facet from the same container.
     *
     * @param facetClass requested facet class
     * @param <T> facet type
     * @return attached facet instance
     */
    protected final <T extends PiStateLivingEntityFacet<?>> T facet(Class<T> facetClass) {
        return context.container().get(facetClass);
    }
}
