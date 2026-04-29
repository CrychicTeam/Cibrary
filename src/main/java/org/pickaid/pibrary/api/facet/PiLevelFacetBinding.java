package org.pickaid.pibrary.api.facet;

import java.util.Objects;
import org.pickaid.pibrary.runtime.facet.PiActiveLevelFacetRegistry;

public final class PiLevelFacetBinding<T extends PiStateLevelFacet<?>> {
    private final Class<T> facetClass;

    public PiLevelFacetBinding(Class<T> facetClass) {
        this.facetClass = Objects.requireNonNull(facetClass, "facetClass");
    }

    public PiLevelFacetType<T> register() {
        return PiActiveLevelFacetRegistry.register(facetClass);
    }
}
