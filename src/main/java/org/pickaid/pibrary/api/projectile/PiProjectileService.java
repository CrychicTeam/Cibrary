package org.pickaid.pibrary.api.projectile;

/**
 * High-level projectile tracing service.
 */
public interface PiProjectileService {
    /**
     * Executes a projectile trace request.
     *
     * @param request immutable trace request
     * @return trace result including the primary hit and collected entity hits
     */
    PiProjectileTraceResult trace(PiProjectileTraceRequest request);
}
