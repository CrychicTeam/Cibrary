package org.pickaid.pibrary.api.service;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.runtime.service.PiActiveLivingServiceRegistry;
import org.pickaid.pibrary.runtime.service.PiLivingQueryHost;

/**
 * Author-facing helpers for resolving living service hosts and instances.
 */
public final class PiLivingServices {
    private PiLivingServices() {
    }

    /**
     * Creates a query host for the given living entity.
     *
     * @param living target living entity
     * @return query host
     */
    public static PiLivingServiceHost host(LivingEntity living) {
        return new PiLivingQueryHost(Objects.requireNonNull(living, "living"));
    }

    /**
     * Creates a registration handle for the given living service type.
     *
     * @param serviceType discovered living service type
     * @param <T> service type
     * @return registration handle
     */
    public static <T extends PiStateLivingEntityService<?>> PiLivingServiceRegistrar<T> host(Class<T> serviceType) {
        return new PiLivingServiceRegistrar<>(serviceType);
    }

    /**
     * Creates a query host when the living entity is present.
     *
     * @param living target living entity
     * @return optional query host
     */
    public static Optional<PiLivingServiceHost> findHost(@Nullable LivingEntity living) {
        return living == null ? Optional.empty() : Optional.of(host(living));
    }

    /**
     * Requires a query host for the given living entity.
     *
     * @param living target living entity
     * @return query host
     */
    public static PiLivingServiceHost requireHost(LivingEntity living) {
        return host(living);
    }

    /**
     * Finds an attached living service by generated descriptor type.
     *
     * @param living owning entity
     * @param serviceType requested service type
     * @param <T> service type
     * @return attached service, if present
     */
    public static <T extends PiStateLivingEntityService<?>> Optional<T> find(LivingEntity living, Class<T> serviceType) {
        return PiActiveLivingServiceRegistry.find(serviceType).flatMap(descriptor -> descriptor.find(living));
    }

    /**
     * Returns the active typed handle for a registered living service.
     *
     * @param serviceType requested service type
     * @param <T> service type
     * @return registered living service type handle
     */
    public static <T extends PiStateLivingEntityService<?>> PiLivingServiceType<T> type(Class<T> serviceType) {
        return PiActiveLivingServiceRegistry.require(serviceType);
    }

    /**
     * Requires an attached living service by registered descriptor type.
     *
     * @param living owning entity
     * @param serviceType requested service type
     * @param <T> service type
     * @return attached service
     */
    public static <T extends PiStateLivingEntityService<?>> T require(LivingEntity living, Class<T> serviceType) {
        return type(serviceType).get(living);
    }
}
