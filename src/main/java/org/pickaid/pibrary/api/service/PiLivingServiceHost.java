package org.pickaid.pibrary.api.service;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryServiceContext;

/**
 * Host abstraction used to resolve living services and shared scoped services.
 */
public interface PiLivingServiceHost {
    /**
     * Returns the owning living entity when available.
     *
     * @return owning entity or {@code null}
     */
    @Nullable
    LivingEntity living();

    /**
     * Returns the host-level shared service registry.
     *
     * @return shared service registry
     */
    PibraryServiceContext services();

    /**
     * Resolves an attached living service from this host.
     *
     * @param serviceType requested service type
     * @param <T> service type
     * @return resolved service
     */
    <T extends PiStateLivingEntityService<?>> T get(Class<T> serviceType);
}
