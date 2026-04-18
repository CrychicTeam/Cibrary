package org.pickaid.pibrary.runtime.service;

/**
 * ServiceLoader entry point that contributes generated living service descriptors.
 */
public interface PiLivingServiceProvider {
    /**
     * Registers generated descriptors into the supplied registry.
     *
     * @param registry descriptor registry
     */
    void register(PiLivingServiceRegistry registry);
}
