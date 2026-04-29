package org.pickaid.pibrary.runtime.facet;

/**
 * ServiceLoader entry point that contributes generated living facet descriptors.
 */
public interface PiLivingFacetProvider {
    /**
     * Registers generated descriptors into the supplied registry.
     *
     * @param registry descriptor registry
     */
    void register(PiLivingFacetRegistry registry);
}
