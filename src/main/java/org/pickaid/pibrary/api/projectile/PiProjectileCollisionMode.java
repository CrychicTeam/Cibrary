package org.pickaid.pibrary.api.projectile;

/**
 * Controls how the projectile tracing service resolves block and entity hits.
 */
public enum PiProjectileCollisionMode {
    STOP_ON_ANY_HIT,
    STOP_ON_BLOCK,
    ENTITY_ONLY,
    PIERCE_ENTITIES
}
