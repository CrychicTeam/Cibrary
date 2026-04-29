package org.pickaid.pibrary.api.facet;

import java.util.Objects;
import org.pickaid.pibrary.runtime.facet.PiActiveLivingFacetRegistry;

public final class PiLivingFacetBinding<T extends PiStateLivingEntityFacet<?>> {
    private final Class<T> facetClass;

    public PiLivingFacetBinding(Class<T> facetClass) {
        this.facetClass = Objects.requireNonNull(facetClass, "facetClass");
    }

    public PiLivingFacetType<T> register() {
        return PiActiveLivingFacetRegistry.register(facetClass);
    }
}
