package org.pickaid.pibrary.runtime.service;

public interface PiLivingServiceRegistry {
    void register(PiGeneratedLivingServiceDescriptor<?, ?> descriptor);
}
