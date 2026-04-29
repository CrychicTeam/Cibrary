package org.pickaid.pibrary.api.projectile;

import net.minecraft.world.entity.projectile.Projectile;

/**
 * Dispatches projectile impact events to handlers registered by projectile type.
 */
public interface PiProjectileImpactRegistry {
    /**
     * Registers a handler for the given projectile type or any of its subclasses.
     *
     * @param projectileType root projectile type to match
     * @param handler impact handler
     * @param <P> supported projectile type
     */
    <P extends Projectile> void register(Class<P> projectileType, PiProjectileImpactHandler<? super P> handler);

    /**
     * Dispatches a projectile impact event.
     *
     * @param projectile projectile that is impacting
     * @param context mutable impact context
     */
    void onImpact(Projectile projectile, PiProjectileImpactContext context);
}
