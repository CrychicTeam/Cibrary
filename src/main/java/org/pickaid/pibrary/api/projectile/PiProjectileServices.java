package org.pickaid.pibrary.api.projectile;

import java.util.Optional;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;

/**
 * Global accessor for the installed {@link PiProjectileService}.
 */
public final class PiProjectileServices {
    public static final PibraryServiceKey<PiProjectileService> KEY =
            new PibraryServiceKey<>(Pibrary.id("projectile_service"), PiProjectileService.class);

    private PiProjectileServices() {
    }

    /**
     * Installs the global projectile tracing service.
     *
     * @param service service instance or {@code null} to remove
     */
    public static void install(PiProjectileService service) {
        PibraryServices.install(KEY, service);
    }

    /**
     * Finds the global projectile tracing service.
     *
     * @return installed service, if present
     */
    public static Optional<PiProjectileService> find() {
        return PibraryServices.findService(KEY);
    }

    /**
     * Requires the global projectile tracing service.
     *
     * @return installed service
     */
    public static PiProjectileService require() {
        return PibraryServices.requireService(KEY);
    }

    /**
     * Executes a trace request using the installed service.
     *
     * @param request immutable trace request
     * @return trace result
     */
    public static PiProjectileTraceResult trace(PiProjectileTraceRequest request) {
        return require().trace(request);
    }
}
