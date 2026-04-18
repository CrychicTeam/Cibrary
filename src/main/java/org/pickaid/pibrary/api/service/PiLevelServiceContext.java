package org.pickaid.pibrary.api.service;

import java.util.Objects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryServiceContext;

/**
 * Immutable construction context passed to level services.
 */
public final class PiLevelServiceContext {
    private final @Nullable ServerLevel level;
    private final PibraryServiceContext sharedServices;
    private final PibraryServiceContext services;

    /**
     * Creates a context with a fresh child service scope derived from the shared level scope.
     *
     * @param level owning level, when available
     * @param sharedServices shared level service registry
     */
    public PiLevelServiceContext(@Nullable ServerLevel level, PibraryServiceContext sharedServices) {
        this(level, sharedServices, sharedServices.child());
    }

    /**
     * Creates a context with an explicit scoped service registry.
     *
     * @param level owning level, when available
     * @param sharedServices shared level service registry
     * @param services scoped registry for the service instance
     */
    public PiLevelServiceContext(@Nullable ServerLevel level, PibraryServiceContext sharedServices, PibraryServiceContext services) {
        this.level = level;
        this.sharedServices = Objects.requireNonNull(sharedServices, "sharedServices");
        this.services = Objects.requireNonNull(services, "services");
    }

    /**
     * Returns the owning server level when available.
     *
     * @return owning level or {@code null}
     */
    public @Nullable ServerLevel level() {
        return level;
    }

    /**
     * Returns the owning server when the level is present.
     *
     * @return owning server or {@code null}
     */
    public @Nullable MinecraftServer server() {
        return level == null ? null : level.getServer();
    }

    /**
     * Returns the scoped service registry owned by this service instance.
     *
     * @return local service registry
     */
    public PibraryServiceContext services() {
        return services;
    }

    /**
     * Returns the shared level-level service registry.
     *
     * @return shared level registry
     */
    public PibraryServiceContext sharedServices() {
        return sharedServices;
    }
}
