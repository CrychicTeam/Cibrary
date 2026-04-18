package org.pickaid.pibrary.api.projectile;

import java.util.List;
import java.util.Objects;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Result of a projectile trace.
 *
 * @param primaryHit primary resolved hit seen by gameplay logic
 * @param entityHits collected entity hits in trace order
 */
public record PiProjectileTraceResult(
        HitResult primaryHit,
        List<EntityHitResult> entityHits
) {
    public PiProjectileTraceResult {
        Objects.requireNonNull(primaryHit, "primaryHit");
        entityHits = entityHits == null ? List.of() : List.copyOf(entityHits);
    }

    public boolean hitEntity() {
        return primaryHit.getType() == HitResult.Type.ENTITY;
    }

    /**
     * Returns whether the primary hit is a block.
     *
     * @return {@code true} when the trace hit a block first
     */
    public boolean hitBlock() {
        return primaryHit.getType() == HitResult.Type.BLOCK;
    }
}
