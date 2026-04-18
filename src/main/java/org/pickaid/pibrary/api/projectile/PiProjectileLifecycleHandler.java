package org.pickaid.pibrary.api.projectile;

import net.minecraft.world.entity.projectile.Projectile;

/**
 * Receives projectile lifecycle callbacks for a registered projectile type.
 *
 * @param <P> supported projectile type
 */
public interface PiProjectileLifecycleHandler<P extends Projectile> {
    /**
     * Called when a matching projectile joins a level.
     *
     * @param projectile matching projectile
     * @param context immutable lifecycle context
     */
    default void onJoinLevel(P projectile, PiProjectileLifecycleContext context) {
    }
}
