package org.pickaid.pibrary.runtime.facet;

/**
 * Mutable registry used while loading generated living facet descriptors.
 */
public interface PiLivingFacetRegistry {
    /**
     * Registers one generated descriptor.
     *
     * @param descriptor generated descriptor
     */
    void register(PiGeneratedLivingFacetDescriptor<?, ?> descriptor);
}
