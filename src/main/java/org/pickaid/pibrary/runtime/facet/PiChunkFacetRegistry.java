package org.pickaid.pibrary.runtime.facet;

public interface PiChunkFacetRegistry {
    void register(PiGeneratedChunkFacetDescriptor<?, ?> descriptor);
}
