package org.pickaid.pibrary.runtime.facet;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryScope;
import org.pickaid.pibrary.api.core.PibraryScopes;
import org.pickaid.pibrary.api.facet.PiLivingFacetContainer;
import org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet;

/**
 * Container implementation used while attaching generated living facets as capabilities.
 */
public final class PiAttachedLivingFacetContainer implements PiLivingFacetContainer {
    private final @Nullable LivingEntity living;
    private final PibraryScope scope;
    private final Map<Class<?>, PiStateLivingEntityFacet<?>> attachedFacets = new ConcurrentHashMap<>();

    /**
     * Creates a container with a fresh child scope.
     *
     * @param living owning entity, when available
     */
    public PiAttachedLivingFacetContainer(@Nullable LivingEntity living) {
        this(living, PibraryScopes.root().child());
    }

    /**
     * Creates a container with an explicit shared scope.
     *
     * @param living owning entity, when available
     * @param scope shared scope
     */
    public PiAttachedLivingFacetContainer(@Nullable LivingEntity living, PibraryScope scope) {
        this.living = living;
        this.scope = Objects.requireNonNull(scope, "scope");
    }

    @Override
    public @Nullable LivingEntity living() {
        return living;
    }

    @Override
    public PibraryScope scope() {
        return scope;
    }

    @Override
    public <T extends PiStateLivingEntityFacet<?>> T get(Class<T> facetClass) {
        PiStateLivingEntityFacet<?> existing = attachedFacets.get(Objects.requireNonNull(facetClass, "facetClass"));
        if (existing != null) {
            return facetClass.cast(existing);
        }
        if (living == null) {
            throw new IllegalStateException("Detached Pi living facet container cannot resolve " + facetClass.getName());
        }
        T resolved = PiLivingFacetDescriptors.requireGenerated(facetClass).require(living);
        PiStateLivingEntityFacet<?> previous = attachedFacets.putIfAbsent(facetClass, resolved);
        return facetClass.cast(previous == null ? resolved : previous);
    }

    /**
     * Attaches an already created facet instance to this container cache.
     *
     * @param facetClass facet class key
     * @param facet facet instance
     * @param <T> facet type
     */
    public <T extends PiStateLivingEntityFacet<?>> void attach(Class<T> facetClass, T facet) {
        Objects.requireNonNull(facetClass, "facetClass");
        Objects.requireNonNull(facet, "facet");
        PiStateLivingEntityFacet<?> previous = attachedFacets.putIfAbsent(facetClass, facet);
        if (previous != null && previous != facet) {
            throw new IllegalStateException("Pi living facet container already attached " + facetClass.getName());
        }
    }
}
