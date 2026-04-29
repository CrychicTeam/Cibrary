package org.pickaid.pibrary.api.registry;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * Immutable summary of a registry plan.
 *
 * @param requestCount total request count
 * @param namespaces namespaces used by request ids in first-seen order
 * @param registries registry ids in first-seen order
 * @param phaseCounts request count by phase
 * @param registryCounts request count by registry id
 * @param groups report groups in first-seen order
 * @param groupCounts request count by group
 */
public record PiRegistryPlanSummary(
        int requestCount,
        List<String> namespaces,
        List<ResourceLocation> registries,
        Map<PiRegistryPhase, Integer> phaseCounts,
        Map<ResourceLocation, Integer> registryCounts,
        List<String> groups,
        Map<String, Integer> groupCounts
) {
    public PiRegistryPlanSummary(
            int requestCount,
            List<String> namespaces,
            List<ResourceLocation> registries,
            Map<PiRegistryPhase, Integer> phaseCounts,
            Map<ResourceLocation, Integer> registryCounts
    ) {
        this(requestCount, namespaces, registries, phaseCounts, registryCounts, List.of(), Map.of());
    }

    public PiRegistryPlanSummary {
        if (requestCount < 0) {
            throw new IllegalArgumentException("requestCount must not be negative");
        }
        namespaces = List.copyOf(Objects.requireNonNull(namespaces, "namespaces"));
        registries = List.copyOf(Objects.requireNonNull(registries, "registries"));
        phaseCounts = Map.copyOf(Objects.requireNonNull(phaseCounts, "phaseCounts"));
        registryCounts = Map.copyOf(Objects.requireNonNull(registryCounts, "registryCounts"));
        groups = List.copyOf(Objects.requireNonNull(groups, "groups"));
        groupCounts = Map.copyOf(Objects.requireNonNull(groupCounts, "groupCounts"));
    }

    /**
     * Returns the count for one phase.
     *
     * @param phase phase to inspect
     * @return request count
     */
    public int count(PiRegistryPhase phase) {
        return phaseCounts.getOrDefault(phase, 0);
    }

    /**
     * Returns the count for one registry.
     *
     * @param registry registry id to inspect
     * @return request count
     */
    public int count(ResourceLocation registry) {
        return registryCounts.getOrDefault(registry, 0);
    }

    /**
     * Returns the count for one report group.
     *
     * @param group group to inspect
     * @return request count
     */
    public int countGroup(String group) {
        return groupCounts.getOrDefault(group, 0);
    }
}
