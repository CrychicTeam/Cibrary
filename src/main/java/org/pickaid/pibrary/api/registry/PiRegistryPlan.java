package org.pickaid.pibrary.api.registry;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * In-memory registry contribution plan with duplicate guards.
 *
 * <p>The plan is useful before Forge or datagen receives anything: contributors
 * can emit requests, tests can inspect the result, and duplicate ids fail close
 * to the authoring code instead of later during event registration.</p>
 */
public final class PiRegistryPlan implements PiRegistrySink {
    private final Map<Key, PiRegistryRequest<?>> requests = new LinkedHashMap<>();
    private final EnumMap<PiRegistryPhase, List<PiRegistryRequest<?>>> byPhase =
            new EnumMap<>(PiRegistryPhase.class);
    private final Map<String, List<PiRegistryRequest<?>>> byGroup = new LinkedHashMap<>();

    public PiRegistryPlan() {
        for (PiRegistryPhase phase : PiRegistryPhase.values()) {
            byPhase.put(phase, new ArrayList<>());
        }
    }

    @Override
    public synchronized <T> void register(PiRegistryRequest<T> request) {
        Objects.requireNonNull(request, "request");
        Key key = Key.of(request);
        PiRegistryRequest<?> previous = requests.putIfAbsent(key, request);
        if (previous != null) {
            throw new IllegalStateException(
                    "Duplicate registry request for " + key.id() + " in registry " + key.registry()
                            + " during " + previous.phase() + " and " + request.phase());
        }
        byPhase.get(request.phase()).add(request);
        byGroup.computeIfAbsent(request.group(), ignored -> new ArrayList<>()).add(request);
    }

    /**
     * Applies a contributor to this plan.
     *
     * @param contributor contributor to run
     * @return this plan
     */
    public PiRegistryPlan accept(PiRegistryContributor contributor) {
        Objects.requireNonNull(contributor, "contributor").contribute(this);
        return this;
    }

    /**
     * Merges another plan into this plan in contribution order.
     *
     * @param other plan to merge
     * @return this plan
     */
    public PiRegistryPlan merge(PiRegistryPlan other) {
        Objects.requireNonNull(other, "other");
        List<PiRegistryRequest<?>> incoming = other.requests();
        synchronized (this) {
            for (PiRegistryRequest<?> request : incoming) {
                Key key = Key.of(request);
                if (requests.containsKey(key)) {
                    PiRegistryRequest<?> previous = requests.get(key);
                    throw new IllegalStateException(
                            "Duplicate registry request for " + key.id() + " in registry " + key.registry()
                                    + " during " + previous.phase() + " and " + request.phase());
                }
            }
            for (PiRegistryRequest<?> request : incoming) {
                registerAny(request);
            }
        }
        return this;
    }

    /**
     * Returns all collected requests in contribution order.
     *
     * @return immutable request list
     */
    public synchronized List<PiRegistryRequest<?>> requests() {
        return List.copyOf(requests.values());
    }

    /**
     * Returns collected requests for one phase.
     *
     * @param phase phase to inspect
     * @return immutable request list
     */
    public synchronized List<PiRegistryRequest<?>> requests(PiRegistryPhase phase) {
        Objects.requireNonNull(phase, "phase");
        return List.copyOf(byPhase.get(phase));
    }

    /**
     * Returns a snapshot summary useful for logs, tests, and diagnostics.
     *
     * @return immutable plan summary
     */
    public synchronized PiRegistryPlanSummary summary() {
        Map<PiRegistryPhase, Integer> phaseCounts = new EnumMap<>(PiRegistryPhase.class);
        Map<ResourceLocation, Integer> registryCounts = new LinkedHashMap<>();
        Map<String, Integer> groupCounts = new LinkedHashMap<>();
        for (PiRegistryPhase phase : PiRegistryPhase.values()) {
            int count = byPhase.get(phase).size();
            if (count > 0) {
                phaseCounts.put(phase, count);
            }
        }
        for (PiRegistryRequest<?> request : requests.values()) {
            registryCounts.merge(request.registryKey().location(), 1, Integer::sum);
            groupCounts.merge(request.group(), 1, Integer::sum);
        }
        return new PiRegistryPlanSummary(
                requests.size(),
                namespaces(),
                List.copyOf(registryCounts.keySet()),
                phaseCounts,
                registryCounts,
                groups(),
                groupCounts);
    }

    /**
     * Returns all namespaces used by collected request ids in first-seen order.
     *
     * @return immutable namespace list
     */
    public synchronized List<String> namespaces() {
        Set<String> namespaces = new LinkedHashSet<>();
        for (PiRegistryRequest<?> request : requests.values()) {
            namespaces.add(request.id().getNamespace());
        }
        return List.copyOf(namespaces);
    }

    /**
     * Returns all report groups used by collected requests in first-seen order.
     *
     * @return immutable group list
     */
    public synchronized List<String> groups() {
        return List.copyOf(byGroup.keySet());
    }

    /**
     * Returns collected requests for one namespace.
     *
     * @param namespace namespace to inspect
     * @return immutable request list
     */
    public synchronized List<PiRegistryRequest<?>> requests(String namespace) {
        Objects.requireNonNull(namespace, "namespace");
        List<PiRegistryRequest<?>> matches = new ArrayList<>();
        for (PiRegistryRequest<?> request : requests.values()) {
            if (request.id().getNamespace().equals(namespace)) {
                matches.add(request);
            }
        }
        return List.copyOf(matches);
    }

    /**
     * Returns collected requests for one report group.
     *
     * @param group group to inspect
     * @return immutable request list
     */
    public synchronized List<PiRegistryRequest<?>> requestsInGroup(String group) {
        Objects.requireNonNull(group, "group");
        return List.copyOf(byGroup.getOrDefault(group, List.of()));
    }

    /**
     * Returns collected requests for one registry.
     *
     * @param registryKey registry to inspect
     * @param <T> registered value type
     * @return immutable typed request list
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> List<PiRegistryRequest<T>> requests(ResourceKey<? extends Registry<T>> registryKey) {
        Objects.requireNonNull(registryKey, "registryKey");
        ResourceLocation registry = registryKey.location();
        List<PiRegistryRequest<T>> matches = new ArrayList<>();
        for (PiRegistryRequest<?> request : requests.values()) {
            if (request.registryKey().location().equals(registry)) {
                matches.add((PiRegistryRequest<T>) request);
            }
        }
        return List.copyOf(matches);
    }

    /**
     * Returns collected requests for one registry and one phase.
     *
     * @param registryKey registry to inspect
     * @param phase phase to inspect
     * @param <T> registered value type
     * @return immutable typed request list
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> List<PiRegistryRequest<T>> requests(
            ResourceKey<? extends Registry<T>> registryKey,
            PiRegistryPhase phase
    ) {
        Objects.requireNonNull(registryKey, "registryKey");
        Objects.requireNonNull(phase, "phase");
        ResourceLocation registry = registryKey.location();
        List<PiRegistryRequest<T>> matches = new ArrayList<>();
        for (PiRegistryRequest<?> request : byPhase.get(phase)) {
            if (request.registryKey().location().equals(registry)) {
                matches.add((PiRegistryRequest<T>) request);
            }
        }
        return List.copyOf(matches);
    }

    /**
     * Checks whether the plan already contains an id in a registry.
     *
     * @param registry registry id
     * @param id entry id
     * @return whether the request exists
     */
    public synchronized boolean contains(ResourceLocation registry, ResourceLocation id) {
        return requests.containsKey(new Key(registry, id));
    }

    /**
     * Checks whether the plan already contains an id in a registry.
     *
     * @param registryKey registry key
     * @param id entry id
     * @return whether the request exists
     */
    public synchronized boolean contains(ResourceKey<? extends Registry<?>> registryKey, ResourceLocation id) {
        Objects.requireNonNull(registryKey, "registryKey");
        return contains(registryKey.location(), id);
    }

    /**
     * Requires every collected request id to use one namespace.
     *
     * @param namespace expected namespace
     * @return this plan
     */
    public synchronized PiRegistryPlan requireNamespace(String namespace) {
        Objects.requireNonNull(namespace, "namespace");
        for (PiRegistryRequest<?> request : requests.values()) {
            if (!request.id().getNamespace().equals(namespace)) {
                throw new IllegalStateException(
                        "Registry request " + request.id() + " does not match namespace " + namespace);
            }
        }
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerAny(PiRegistryRequest<?> request) {
        register((PiRegistryRequest) request);
    }

    private record Key(ResourceLocation registry, ResourceLocation id) {
        private static Key of(PiRegistryRequest<?> request) {
            return new Key(request.registryKey().location(), request.id());
        }
    }
}
