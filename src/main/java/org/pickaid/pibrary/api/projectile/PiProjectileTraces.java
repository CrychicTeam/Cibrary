package org.pickaid.pibrary.api.projectile;

import java.util.Optional;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryScopeKey;
import org.pickaid.pibrary.api.core.PibraryScopes;

/**
 * Global accessor for the installed {@link PiProjectileTracer}.
 */
public final class PiProjectileTraces {
    public static final PibraryScopeKey<PiProjectileTracer> KEY =
            new PibraryScopeKey<>(Pibrary.id("projectile_tracer"), PiProjectileTracer.class);

    private PiProjectileTraces() {
    }

    /**
     * Installs the global projectile tracer.
     *
     * @param tracer tracer instance or {@code null} to remove
     */
    public static void install(PiProjectileTracer tracer) {
        PibraryScopes.install(KEY, tracer);
    }

    /**
     * Finds the global projectile tracer.
     *
     * @return installed tracer, if present
     */
    public static Optional<PiProjectileTracer> find() {
        return PibraryScopes.findGlobal(KEY);
    }

    /**
     * Requires the global projectile tracer.
     *
     * @return installed tracer
     */
    public static PiProjectileTracer require() {
        return PibraryScopes.requireGlobal(KEY);
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
