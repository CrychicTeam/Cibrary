package org.pickaid.pibrary.api.projectile;

import java.util.Objects;
import net.minecraft.world.phys.HitResult;

/**
 * Mutable decision object passed to projectile impact handlers.
 */
public final class PiProjectileImpactContext {
    private final HitResult hitResult;
    private PiProjectileImpactResult result = PiProjectileImpactResult.DEFAULT;
    private boolean canceled;

    /**
     * Creates a new impact context for the current hit result.
     *
     * @param hitResult vanilla hit result being processed
     */
    public PiProjectileImpactContext(HitResult hitResult) {
        this.hitResult = Objects.requireNonNull(hitResult, "hitResult");
    }

    /**
     * Returns the raw vanilla hit result.
     *
     * @return current hit result
     */
    public HitResult hitResult() {
        return hitResult;
    }

    /**
     * Returns the current impact policy selected by handlers.
     *
     * @return selected impact result
     */
    public PiProjectileImpactResult result() {
        return result;
    }

    /**
     * Updates the impact policy that should be applied after all handlers run.
     *
     * @param result selected impact result
     */
    public void setResult(PiProjectileImpactResult result) {
        this.result = Objects.requireNonNull(result, "result");
    }

    /**
     * Returns whether the underlying Forge impact event should be canceled.
     *
     * @return {@code true} when the projectile should continue flying
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Marks the underlying Forge impact event as canceled or not.
     *
     * @param canceled whether to cancel the impact
     */
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    /**
     * Convenience helper that cancels the impact and leaves the projectile flying.
     */
    public void cancelImpact() {
        setCanceled(true);
    }
}
