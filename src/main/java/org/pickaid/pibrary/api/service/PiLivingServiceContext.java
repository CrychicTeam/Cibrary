package org.pickaid.pibrary.api.service;

import java.util.Objects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryServiceContext;

/**
 * Immutable construction context passed to living services.
 */
public final class PiLivingServiceContext {
    private final @Nullable LivingEntity living;
    private final PiLivingServiceHost host;
    private final PibraryServiceContext services;

    /**
     * Creates a context with a fresh child service scope derived from the host.
     *
     * @param living owning entity, when available
     * @param host owning host
     */
    public PiLivingServiceContext(@Nullable LivingEntity living, PiLivingServiceHost host) {
        this(living, host, host.services().child());
    }

    /**
     * Creates a context with an explicit scoped service registry.
     *
     * @param living owning entity, when available
     * @param host owning host
     * @param services scoped registry for the service instance
     */
    public PiLivingServiceContext(@Nullable LivingEntity living, PiLivingServiceHost host, PibraryServiceContext services) {
        this.living = living;
        this.host = Objects.requireNonNull(host, "host");
        this.services = Objects.requireNonNull(services, "services");
    }

    /**
     * Returns the owning living entity when available.
     *
     * @return owning entity or {@code null}
     */
    public @Nullable LivingEntity living() {
        return living;
    }

    /**
     * Returns the owning player when the host entity is a player.
     *
     * @return owning player or {@code null}
     */
    public @Nullable Player player() {
        return living instanceof Player player ? player : null;
    }

    /**
     * Returns the host used to resolve peer services.
     *
     * @return owning host
     */
    public PiLivingServiceHost host() {
        return host;
    }

    /**
     * Returns the scoped service registry local to this service instance.
     *
     * @return local service registry
     */
    public PibraryServiceContext services() {
        return services;
    }

    /**
     * Returns the shared host-level service registry.
     *
     * @return shared host registry
     */
    public PibraryServiceContext sharedServices() {
        return host.services();
    }
}
