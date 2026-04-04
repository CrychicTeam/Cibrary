package org.pickaid.pibrary.api.registry;

@FunctionalInterface
public interface PiRegistryContributor {
    void contribute(PiRegistrySink sink);
}
