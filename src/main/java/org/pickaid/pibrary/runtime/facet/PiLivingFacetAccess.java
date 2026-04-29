package org.pickaid.pibrary.runtime.facet;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryScope;
import org.pickaid.pibrary.api.core.PibraryScopes;
import org.pickaid.pibrary.api.facet.PiLivingFacetContainer;
import org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet;

/**
 * Read-oriented container that resolves facets directly from attached capabilities.
 */
public final class PiLivingFacetAccess implements PiLivingFacetContainer {
    private final @Nullable LivingEntity living;
    private final PibraryScope scope;

    /**
     * Creates a query container with a fresh child scope.
     *
     * @param living owning entity, when available
     */
    public PiLivingFacetAccess(@Nullable LivingEntity living) {
        this(living, PibraryScopes.root().child());
    }

    /**
     * Creates a query container with an explicit shared scope.
     *
     * @param living owning entity, when available
     * @param scope shared scope
     */
    public PiLivingFacetAccess(@Nullable LivingEntity living, PibraryScope scope) {
        this.living = living;
        this.scope = scope;
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
        if (living == null) {
            throw new IllegalStateException("Detached Pi living facet container cannot resolve " + facetClass.getName());
        }
        return PiLivingFacetDescriptors.requireGenerated(facetClass).require(living);
    }
}
