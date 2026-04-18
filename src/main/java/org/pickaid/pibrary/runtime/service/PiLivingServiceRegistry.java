package org.pickaid.pibrary.runtime.service;

/**
 * Mutable registry used while loading generated living service descriptors.
 */
public interface PiLivingServiceRegistry {
    /**
     * Registers one generated descriptor.
     *
     * @param descriptor generated descriptor
     */
    void register(PiGeneratedLivingServiceDescriptor<?, ?> descriptor);
}
