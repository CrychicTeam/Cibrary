package org.pickaid.pibrary.runtime.registry;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegisterEvent;
import org.pickaid.pibrary.api.registry.PiRegistryPhase;
import org.pickaid.pibrary.api.registry.PiRegistryPlan;
import org.pickaid.pibrary.api.registry.PiRegistryRequest;

/**
 * Applies registry plans to Forge registration events.
 */
public final class PiForgeRegistryPlans {
    private PiForgeRegistryPlans() {
    }

    /**
     * Registers all mod-event requests matching the current Forge register event.
     *
     * @param event Forge register event
     * @param plan registry plan to apply
     * @return number of requests matched for this event
     */
    public static int apply(RegisterEvent event, PiRegistryPlan plan) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(plan, "plan");
        return apply(event, plan, PiRegistryPhase.MOD_EVENT_REGISTRATION);
    }

    /**
     * Registers all requests matching the current Forge register event and phase.
     *
     * @param event Forge register event
     * @param plan registry plan to apply
     * @param phase phase to apply
     * @return number of requests matched for this event and phase
     */
    public static int apply(RegisterEvent event, PiRegistryPlan plan, PiRegistryPhase phase) {
        return applyDetailed(event, plan, phase).appliedRequests();
    }

    /**
     * Registers all mod-event requests after requiring all ids to use one
     * namespace.
     *
     * @param event Forge register event
     * @param plan registry plan to apply
     * @param namespace required namespace
     * @return number of requests matched for this event
     */
    public static int apply(RegisterEvent event, PiRegistryPlan plan, String namespace) {
        return applyDetailed(event, plan, namespace).appliedRequests();
    }

    /**
     * Registers all requests for one phase after requiring all ids to use one
     * namespace.
     *
     * @param event Forge register event
     * @param plan registry plan to apply
     * @param phase phase to apply
     * @param namespace required namespace
     * @return number of requests matched for this event and phase
     */
    public static int apply(RegisterEvent event, PiRegistryPlan plan, PiRegistryPhase phase, String namespace) {
        return applyDetailed(event, plan, phase, namespace).appliedRequests();
    }

    /**
     * Registers all mod-event requests matching the current Forge register event
     * and returns a report suitable for logs or diagnostics.
     *
     * @param event Forge register event
     * @param plan registry plan to apply
     * @return apply report
     */
    public static PiRegistryApplyReport applyDetailed(RegisterEvent event, PiRegistryPlan plan) {
        return applyDetailed(event, plan, PiRegistryPhase.MOD_EVENT_REGISTRATION);
    }

    /**
     * Registers all mod-event requests after requiring all ids to use one
     * namespace, then returns a report suitable for logs or diagnostics.
     *
     * @param event Forge register event
     * @param plan registry plan to apply
     * @param namespace required namespace
     * @return apply report
     */
    public static PiRegistryApplyReport applyDetailed(RegisterEvent event, PiRegistryPlan plan, String namespace) {
        return applyDetailed(event, plan, PiRegistryPhase.MOD_EVENT_REGISTRATION, namespace);
    }

    /**
     * Registers all requests for one phase after requiring all ids to use one
     * namespace, then returns a report suitable for logs or diagnostics.
     *
     * @param event Forge register event
     * @param plan registry plan to apply
     * @param phase phase to apply
     * @param namespace required namespace
     * @return apply report
     */
    public static PiRegistryApplyReport applyDetailed(
            RegisterEvent event,
            PiRegistryPlan plan,
            PiRegistryPhase phase,
            String namespace
    ) {
        Objects.requireNonNull(plan, "plan").requireNamespace(namespace);
        return applyDetailed(event, plan, phase);
    }

    /**
     * Registers all requests matching the current Forge register event and phase,
     * then returns a report suitable for logs or diagnostics.
     *
     * @param event Forge register event
     * @param plan registry plan to apply
     * @param phase phase to apply
     * @return apply report
     */
    public static PiRegistryApplyReport applyDetailed(RegisterEvent event, PiRegistryPlan plan, PiRegistryPhase phase) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(phase, "phase");
        ResourceKey<? extends Registry<Object>> registryKey = registryKey(event);
        List<? extends PiRegistryRequest<?>> requests = plan.requests(registryKey, phase);
        List<ResourceLocation> appliedIds = requests.stream()
                .map(PiRegistryRequest::id)
                .toList();
        Set<String> appliedGroups = new LinkedHashSet<>();
        for (PiRegistryRequest<?> request : requests) {
            appliedGroups.add(request.group());
            register(event, request);
        }
        return new PiRegistryApplyReport(
                registryKey.location(),
                phase,
                appliedIds,
                List.copyOf(appliedGroups),
                plan.requests().size() - requests.size());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> void register(RegisterEvent event, PiRegistryRequest<T> request) {
        event.register((ResourceKey) request.registryKey(), request.id(), () -> request.factory().get());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ResourceKey<? extends Registry<Object>> registryKey(RegisterEvent event) {
        return (ResourceKey) event.getRegistryKey();
    }
}
