package org.pickaid.pibrary.api.service;

import java.util.Objects;
import java.util.Optional;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServiceKey;

/**
 * Base class for living-entity-scoped services.
 */
public abstract class PiLivingEntityService {
    private final PiLivingServiceContext context;

    protected PiLivingEntityService(PiLivingServiceContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    /**
     * Returns the immutable service context.
     *
     * @return service context
     */
    public final PiLivingServiceContext context() {
        return context;
    }

    /**
     * Returns the scoped service registry owned by this service instance.
     *
     * @return scoped service registry
     */
    protected final PibraryServiceContext services() {
        return context.services();
    }

    /**
     * Returns the shared host-level service registry.
     *
     * @return shared host service registry
     */
    protected final PibraryServiceContext sharedServices() {
        return context.sharedServices();
    }

    /**
     * Finds another service in the local scoped registry.
     *
     * @param key service key
     * @param <T> service type
     * @return resolved service, if present
     */
    protected final <T> Optional<T> findService(PibraryServiceKey<T> key) {
        return services().find(key);
    }

    /**
     * Requires another service from the local scoped registry.
     *
     * @param key service key
     * @param <T> service type
     * @return resolved service
     */
    protected final <T> T requireService(PibraryServiceKey<T> key) {
        return services().require(key);
    }

    /**
     * Resolves another attached living service from the same host.
     *
     * @param serviceType requested service type
     * @param <T> service type
     * @return attached service instance
     */
    protected final <T extends PiStateLivingEntityService<?>> T service(Class<T> serviceType) {
        return context.host().get(serviceType);
    }
}
