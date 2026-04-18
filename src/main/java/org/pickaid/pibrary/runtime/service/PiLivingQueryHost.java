package org.pickaid.pibrary.runtime.service;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServices;
import org.pickaid.pibrary.api.service.PiLivingServiceHost;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;

/**
 * Read-oriented host that resolves services directly from attached capabilities.
 */
public final class PiLivingQueryHost implements PiLivingServiceHost {
    private final @Nullable LivingEntity living;
    private final PibraryServiceContext services;

    /**
     * Creates a query host with a fresh child service scope.
     *
     * @param living owning entity, when available
     */
    public PiLivingQueryHost(@Nullable LivingEntity living) {
        this(living, PibraryServices.root().child());
    }

    /**
     * Creates a query host with an explicit shared service scope.
     *
     * @param living owning entity, when available
     * @param services shared service scope
     */
    public PiLivingQueryHost(@Nullable LivingEntity living, PibraryServiceContext services) {
        this.living = living;
        this.services = services;
    }

    @Override
    public @Nullable LivingEntity living() {
        return living;
    }

    @Override
    public PibraryServiceContext services() {
        return services;
    }

    @Override
    public <T extends PiStateLivingEntityService<?>> T get(Class<T> serviceType) {
        if (living == null) {
            throw new IllegalStateException("Detached Pi living query host cannot resolve " + serviceType.getName());
        }
        return PiLivingServiceDescriptors.requireGenerated(serviceType).require(living);
    }
}
