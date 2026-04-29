package org.pickaid.pibrary.api.facet;

import org.pickaid.pibrary.runtime.facet.PiActiveChunkFacetRegistry;

public final class PiChunkFacetBinding<T extends PiStateChunkFacet<?>> {
    private final Class<T> facetClass;

    public PiChunkFacetBinding(Class<T> facetClass) {
        this.facetClass = facetClass;
    }

    public PiChunkFacetType<T> register() {
        return PiActiveChunkFacetRegistry.register(facetClass);
    }
}
