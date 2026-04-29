package org.pickaid.pibrary.api.registry;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Typed helper for one registry family.
 *
 * <p>A family is the small piece most mods actually want to keep around:
 * "these entries all go into the same registry and usually share my mod id".
 * It does not own Forge event wiring. It only creates validated
 * {@link PiRegistryRequest} objects that a runtime adapter can later apply.</p>
 *
 * @param registryKey target registry
 * @param namespace default namespace used by {@link #entry(String, Supplier)}
 * @param defaultPhase default phase for entries
 * @param defaultGroup default report group for entries
 * @param <T> registered value type
 */
public record PiRegistryFamily<T>(
        ResourceKey<? extends Registry<T>> registryKey,
        String namespace,
        PiRegistryPhase defaultPhase,
        String defaultGroup
) {
    public PiRegistryFamily(
            ResourceKey<? extends Registry<T>> registryKey,
            String namespace,
            PiRegistryPhase defaultPhase
    ) {
        this(registryKey, namespace, defaultPhase, PiRegistryRequest.DEFAULT_GROUP);
    }

    public PiRegistryFamily {
        Objects.requireNonNull(registryKey, "registryKey");
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(defaultPhase, "defaultPhase");
        Objects.requireNonNull(defaultGroup, "defaultGroup");
        if (namespace.isBlank()) {
            throw new IllegalArgumentException("namespace must not be blank");
        }
        if (defaultGroup.isBlank()) {
            throw new IllegalArgumentException("defaultGroup must not be blank");
        }
    }

    /**
     * Creates a request under this family's default namespace and phase.
     *
     * @param path path inside the namespace
     * @param factory value factory
     * @return registry request
     */
    public PiRegistryRequest<T> entry(String path, Supplier<? extends T> factory) {
        return entry(ResourceLocation.fromNamespaceAndPath(namespace, path), factory, defaultPhase);
    }

    /**
     * Creates a request under this family's default namespace and a specific
     * phase.
     *
     * @param path path inside the namespace
     * @param factory value factory
     * @param phase registration phase
     * @return registry request
     */
    public PiRegistryRequest<T> entry(String path, Supplier<? extends T> factory, PiRegistryPhase phase) {
        return entry(ResourceLocation.fromNamespaceAndPath(namespace, path), factory, phase);
    }

    /**
     * Creates a request with an explicit id.
     *
     * @param id full registry id
     * @param factory value factory
     * @param phase registration phase
     * @return registry request
     */
    public PiRegistryRequest<T> entry(ResourceLocation id, Supplier<? extends T> factory, PiRegistryPhase phase) {
        return new PiRegistryRequest<>(registryKey, id, factory, phase, defaultGroup);
    }

    /**
     * Emits one request directly into a sink.
     *
     * @param sink registry sink
     * @param path path inside the namespace
     * @param factory value factory
     */
    public void register(PiRegistrySink sink, String path, Supplier<? extends T> factory) {
        Objects.requireNonNull(sink, "sink").register(entry(path, factory));
    }

    /**
     * Returns the same family with a different default phase.
     *
     * @param phase new default phase
     * @return family copy
     */
    public PiRegistryFamily<T> withDefaultPhase(PiRegistryPhase phase) {
        return new PiRegistryFamily<>(registryKey, namespace, phase, defaultGroup);
    }

    /**
     * Returns the same family with a different report group.
     *
     * @param group group name used by registry plans
     * @return family copy
     */
    public PiRegistryFamily<T> withDefaultGroup(String group) {
        return new PiRegistryFamily<>(registryKey, namespace, defaultPhase, group);
    }
}
