package org.pickaid.pibrary.api.projectile;

/**
 * Abstract impact decisions exposed by Pibrary and mapped to Forge at runtime.
 */
public enum PiProjectileImpactResult {
    DEFAULT,
    SKIP_ENTITY,
    STOP_AT_CURRENT,
    STOP_AT_CURRENT_NO_DAMAGE
}
