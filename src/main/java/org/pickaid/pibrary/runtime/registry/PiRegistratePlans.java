package org.pickaid.pibrary.runtime.registry;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.pickaid.pibrary.api.registry.PiRegistryPhase;
import org.pickaid.pibrary.api.registry.PiRegistryPlan;
import org.pickaid.pibrary.api.registry.PiRegistryRequest;

/**
 * Applies registry plans to Registrate's simple registration path.
 */
public final class PiRegistratePlans {
    private PiRegistratePlans() {
    }

    /**
     * Registers all mod-event requests through Registrate's generic simple
     * registration path.
     *
     * @param registrate target Registrate instance
     * @param plan registry plan to apply
     * @return entries returned by Registrate
     */
    public static List<RegistryEntry<?>> simple(AbstractRegistrate<?> registrate, PiRegistryPlan plan) {
        return simple(registrate, plan, PiRegistryPhase.MOD_EVENT_REGISTRATION);
    }

    /**
     * Registers all requests for one phase through Registrate's generic simple
     * registration path.
     *
     * @param registrate target Registrate instance
     * @param plan registry plan to apply
     * @param phase phase to apply
     * @return entries returned by Registrate
     */
    public static List<RegistryEntry<?>> simple(AbstractRegistrate<?> registrate, PiRegistryPlan plan, PiRegistryPhase phase) {
        Objects.requireNonNull(registrate, "registrate");
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(phase, "phase");
        plan.requireNamespace(registrate.getModid());
        List<PiRegistryRequest<?>> requests = plan.requests(phase);

        List<RegistryEntry<?>> entries = new ArrayList<>(requests.size());
        for (PiRegistryRequest<?> request : requests) {
            entries.add(simple(registrate, request));
        }
        return List.copyOf(entries);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static RegistryEntry<?> simple(AbstractRegistrate<?> registrate, PiRegistryRequest<?> request) {
        ResourceKey key = (ResourceKey<? extends Registry<?>>) request.registryKey();
        return ((AbstractRegistrate) registrate).simple(request.id().getPath(), key, () -> request.factory().get());
    }
}
