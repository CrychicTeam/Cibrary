package org.pickaid.pibrary.runtime.registry;

import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.registry.PiRegistryPhase;

/**
 * Summary produced after a registry plan adapter applies one phase.
 *
 * @param registry registry handled by the adapter call
 * @param phase phase handled by the adapter call
 * @param appliedIds ids applied by this call
 * @param appliedGroups groups represented by applied ids
 * @param skippedRequests plan requests skipped because they belonged to another
 *                        registry or phase
 */
public record PiRegistryApplyReport(
        ResourceLocation registry,
        PiRegistryPhase phase,
        List<ResourceLocation> appliedIds,
        List<String> appliedGroups,
        int skippedRequests
) {
    public PiRegistryApplyReport(
            ResourceLocation registry,
            PiRegistryPhase phase,
            List<ResourceLocation> appliedIds,
            int skippedRequests
    ) {
        this(registry, phase, appliedIds, List.of(), skippedRequests);
    }

    public PiRegistryApplyReport {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(phase, "phase");
        appliedIds = List.copyOf(Objects.requireNonNull(appliedIds, "appliedIds"));
        appliedGroups = List.copyOf(Objects.requireNonNull(appliedGroups, "appliedGroups"));
        if (skippedRequests < 0) {
            throw new IllegalArgumentException("skippedRequests must not be negative");
        }
    }

    /**
     * Returns how many requests were applied.
     *
     * @return applied request count
     */
    public int appliedRequests() {
        return appliedIds.size();
    }
}
