package org.pickaid.pibrary.runtime.registry;

import java.util.Objects;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;
import org.pickaid.pibrary.api.registry.PiRegistryPlan;
import org.pickaid.pibrary.api.registry.PiRegistryPlanSummary;

/**
 * Small Forge bootstrap object for applying one namespace-checked registry plan.
 */
public final class PiForgeRegistryBootstrap {
    private final PiRegistryPlan plan;
    private final String namespace;
    private final PiRegistryPlanSummary summary;

    private PiForgeRegistryBootstrap(PiRegistryPlan plan, String namespace) {
        this.plan = Objects.requireNonNull(plan, "plan").requireNamespace(namespace);
        this.namespace = Objects.requireNonNull(namespace, "namespace");
        this.summary = plan.summary();
    }

    /**
     * Creates a Forge bootstrap for one registry plan and namespace.
     *
     * @param plan registry plan
     * @param namespace expected namespace
     * @return bootstrap wrapper
     */
    public static PiForgeRegistryBootstrap create(PiRegistryPlan plan, String namespace) {
        return new PiForgeRegistryBootstrap(plan, namespace);
    }

    /**
     * Adds this bootstrap to a Forge mod event bus.
     *
     * @param modBus mod event bus
     */
    public void register(IEventBus modBus) {
        Objects.requireNonNull(modBus, "modBus").addListener(this::handle);
    }

    /**
     * Handles one Forge register event.
     *
     * @param event register event
     * @return apply report
     */
    public PiRegistryApplyReport handle(RegisterEvent event) {
        return PiForgeRegistryPlans.applyDetailed(event, plan, namespace);
    }

    /**
     * Returns the plan owned by this bootstrap.
     *
     * @return registry plan
     */
    public PiRegistryPlan plan() {
        return plan;
    }

    /**
     * Returns the namespace required by this bootstrap.
     *
     * @return namespace
     */
    public String namespace() {
        return namespace;
    }

    /**
     * Returns the plan summary captured at creation time.
     *
     * @return plan summary
     */
    public PiRegistryPlanSummary summary() {
        return summary;
    }
}
