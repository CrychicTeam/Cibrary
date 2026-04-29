package org.pickaid.pibrary.api.facet;

import java.util.Objects;
import java.util.Optional;
import org.pickaid.pibrary.api.core.PibraryScope;
import org.pickaid.pibrary.api.core.PibraryScopeKey;

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
     * Returns the scope owned by this facet instance.
     *
     * @return local scope
     */
    protected final PibraryScope scope() {
        return context.scope();
    }

    /**
     * Returns the shared container-level scope.
     *
     * @return shared container scope
     */
    protected final PibraryScope sharedScope() {
        return context.sharedScope();
    }

    /**
     * Finds another value in the local scoped registry.
     *
     * @param key scope key
     * @param <T> scope type
     * @return resolved value, if present
     */
    protected final <T> Optional<T> findScoped(PibraryScopeKey<T> key) {
        return scope().find(key);
    }

    /**
     * Requires another value from the local scoped registry.
     *
     * @param key scope key
     * @param <T> scope type
     * @return resolved value
     */
    protected final <T> T requireScoped(PibraryScopeKey<T> key) {
        return scope().require(key);
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
