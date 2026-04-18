package org.pickaid.pibrary.api.projectile;

/**
 * Immutable join-level context for projectile lifecycle callbacks.
 *
 * @param loadedFromDisk whether the projectile came from saved data instead of a live spawn
 */
public record PiProjectileLifecycleContext(boolean loadedFromDisk) {
}
