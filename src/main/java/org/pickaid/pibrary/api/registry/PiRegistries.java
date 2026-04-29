package org.pickaid.pibrary.api.registry;

import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Entry points for Pibrary registry authoring helpers.
 */
public final class PiRegistries {
    private PiRegistries() {
    }

    /**
     * Creates a typed family using {@link PiRegistryPhase#MOD_EVENT_REGISTRATION}
     * as its default phase.
     *
     * @param registryKey target registry
     * @param namespace default namespace
     * @param <T> registered value type
     * @return registry family
     */
    public static <T> PiRegistryFamily<T> family(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        return new PiRegistryFamily<>(registryKey, namespace, PiRegistryPhase.MOD_EVENT_REGISTRATION);
    }

    /**
     * Creates an empty registry plan.
     *
     * @return mutable plan
     */
    public static PiRegistryPlan plan() {
        return new PiRegistryPlan();
    }

    /**
     * Collects contributors into a new plan.
     *
     * @param contributors contributors to run
     * @return populated plan
     */
    public static PiRegistryPlan collect(PiRegistryContributor... contributors) {
        Objects.requireNonNull(contributors, "contributors");
        PiRegistryPlan plan = plan();
        for (PiRegistryContributor contributor : contributors) {
            plan.accept(contributor);
        }
        return plan;
    }
}
