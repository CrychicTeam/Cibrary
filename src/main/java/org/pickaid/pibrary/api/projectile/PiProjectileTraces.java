package org.pickaid.pibrary.api.projectile;

import java.util.Optional;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;

/**
 * Global accessor for the installed {@link PiProjectileTracer}.
 */
public final class PiProjectileTraces {
    public static final PibraryServiceKey<PiProjectileTracer> KEY =
            new PibraryServiceKey<>(Pibrary.id("projectile_tracer"), PiProjectileTracer.class);

    private PiProjectileTraces() {
    }

    /**
     * Installs the global projectile tracer.
     *
     * @param tracer tracer instance or {@code null} to remove
     */
    public static void install(PiProjectileTracer tracer) {
        PibraryServices.install(KEY, tracer);
    }

    /**
     * Finds the global projectile tracer.
     *
     * @return installed tracer, if present
     */
    public static Optional<PiProjectileTracer> find() {
        return PibraryServices.findService(KEY);
    }

    /**
     * Requires the global projectile tracer.
     *
     * @return installed tracer
     */
    public static PiProjectileTracer require() {
        return PibraryServices.requireService(KEY);
    }

    /**
     * Executes a trace request using the installed tracer.
     *
     * @param request immutable trace request
     * @return trace result
     */
    public static PiProjectileTraceResult trace(PiProjectileTraceRequest request) {
        return require().trace(request);
    }
}
