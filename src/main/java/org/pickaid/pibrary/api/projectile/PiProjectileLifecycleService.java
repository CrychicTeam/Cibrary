package org.pickaid.pibrary.api.projectile;

import net.minecraft.world.entity.projectile.Projectile;

/**
 * Dispatches projectile lifecycle events to handlers registered by projectile type.
 */
public interface PiProjectileLifecycleService {
    /**
     * Registers a handler for the given projectile type or any of its subclasses.
     *
     * @param projectileType root projectile type to match
     * @param handler lifecycle handler
     * @param <P> supported projectile type
     */
    <P extends Projectile> void register(Class<P> projectileType, PiProjectileLifecycleHandler<? super P> handler);

    /**
     * Dispatches a projectile join-level event.
     *
     * @param projectile projectile that joined a level
     * @param context immutable lifecycle context
     */
    void onJoinLevel(Projectile projectile, PiProjectileLifecycleContext context);
}
