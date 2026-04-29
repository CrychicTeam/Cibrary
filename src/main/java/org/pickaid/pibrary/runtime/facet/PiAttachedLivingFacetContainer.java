package org.pickaid.pibrary.runtime.facet;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServices;
import org.pickaid.pibrary.api.facet.PiLivingFacetContainer;
import org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet;

/**
 * Container implementation used while attaching generated living facets as capabilities.
 */
public final class PiAttachedLivingFacetContainer implements PiLivingFacetContainer {
    private final @Nullable LivingEntity living;
    private final PibraryServiceContext services;
    private final Map<Class<?>, PiStateLivingEntityFacet<?>> attachedFacets = new ConcurrentHashMap<>();

    /**
     * Creates a container with a fresh child service scope.
     *
     * @param living owning entity, when available
     */
    public PiAttachedLivingFacetContainer(@Nullable LivingEntity living) {
        this(living, PibraryServices.root().child());
    }

    /**
     * Creates a container with an explicit shared service scope.
     *
     * @param living owning entity, when available
     * @param services shared service scope
     */
    public PiAttachedLivingFacetContainer(@Nullable LivingEntity living, PibraryServiceContext services) {
        this.living = living;
        this.services = Objects.requireNonNull(services, "services");
    }

    @Override
    public @Nullable LivingEntity living() {
        return living;
    }

    @Override
    public PibraryServiceContext services() {
        return services;
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
