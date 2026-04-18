package org.pickaid.pibrary.runtime.service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServices;
import org.pickaid.pibrary.api.service.PiLivingServiceHost;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;

/**
 * Host implementation used while attaching generated living services as capabilities.
 */
public final class PiAttachedLivingHost implements PiLivingServiceHost {
    private final @Nullable LivingEntity living;
    private final PibraryServiceContext services;
    private final Map<Class<?>, PiStateLivingEntityService<?>> attachedServices = new ConcurrentHashMap<>();

    /**
     * Creates a host with a fresh child service scope.
     *
     * @param living owning entity, when available
     */
    public PiAttachedLivingHost(@Nullable LivingEntity living) {
        this(living, PibraryServices.root().child());
    }

    /**
     * Creates a host with an explicit shared service scope.
     *
     * @param living owning entity, when available
     * @param services shared service scope
     */
    public PiAttachedLivingHost(@Nullable LivingEntity living, PibraryServiceContext services) {
        this.living = living;
        this.services = Objects.requireNonNull(services, "services");
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
        PiStateLivingEntityService<?> existing = attachedServices.get(Objects.requireNonNull(serviceType, "serviceType"));
        if (existing != null) {
            return serviceType.cast(existing);
        }
        if (living == null) {
            throw new IllegalStateException("Detached Pi living host cannot resolve " + serviceType.getName());
        }
        T resolved = PiLivingServiceDescriptors.requireGenerated(serviceType).require(living);
        PiStateLivingEntityService<?> previous = attachedServices.putIfAbsent(serviceType, resolved);
        return serviceType.cast(previous == null ? resolved : previous);
    }

    /**
     * Attaches an already created service instance to this host cache.
     *
     * @param serviceType service type key
     * @param service service instance
     * @param <T> service type
     */
    public <T extends PiStateLivingEntityService<?>> void attach(Class<T> serviceType, T service) {
        Objects.requireNonNull(serviceType, "serviceType");
        Objects.requireNonNull(service, "service");
        PiStateLivingEntityService<?> previous = attachedServices.putIfAbsent(serviceType, service);
        if (previous != null && previous != service) {
            throw new IllegalStateException("Pi living host already attached service " + serviceType.getName());
        }
    }
}
