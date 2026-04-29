package org.pickaid.pibrary.runtime.facet;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServices;
import org.pickaid.pibrary.api.facet.PiLivingFacetContainer;
import org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet;

/**
 * Read-oriented container that resolves facets directly from attached capabilities.
 */
public final class PiLivingFacetAccess implements PiLivingFacetContainer {
    private final @Nullable LivingEntity living;
    private final PibraryServiceContext services;

    /**
     * Creates a query container with a fresh child service scope.
     *
     * @param living owning entity, when available
     */
    public PiLivingFacetAccess(@Nullable LivingEntity living) {
        this(living, PibraryServices.root().child());
    }

    /**
     * Creates a query container with an explicit shared service scope.
     *
     * @param living owning entity, when available
     * @param services shared service scope
     */
    public PiLivingFacetAccess(@Nullable LivingEntity living, PibraryServiceContext services) {
        this.living = living;
        this.services = services;
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
        if (living == null) {
            throw new IllegalStateException("Detached Pi living facet container cannot resolve " + facetClass.getName());
        }
        return PiLivingFacetDescriptors.requireGenerated(facetClass).require(living);
    }
}
