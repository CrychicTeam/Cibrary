package org.pickaid.pibrary.api.registry;

public interface PiRegistrySink {
    <T> void register(PiRegistryRequest<T> request);
}
