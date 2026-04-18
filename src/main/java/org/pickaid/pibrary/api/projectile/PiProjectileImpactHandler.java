package org.pickaid.pibrary.api.projectile;

import net.minecraft.world.entity.projectile.Projectile;

/**
 * Receives projectile impact callbacks for a registered projectile type.
 *
 * @param <P> supported projectile type
 */
public interface PiProjectileImpactHandler<P extends Projectile> {
    /**
     * Called when a matching projectile is about to process an impact.
     *
     * @param projectile matching projectile
     * @param context mutable impact context
     */
    default void onImpact(P projectile, PiProjectileImpactContext context) {
    }
}
