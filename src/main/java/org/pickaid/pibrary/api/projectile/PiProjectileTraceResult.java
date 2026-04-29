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
 * @param grazeHits near-miss entity hits collected outside the real hit radius
 */
public record PiProjectileTraceResult(
        HitResult primaryHit,
        List<EntityHitResult> entityHits,
        List<EntityHitResult> grazeHits
) {
    public PiProjectileTraceResult(HitResult primaryHit, List<EntityHitResult> entityHits) {
        this(primaryHit, entityHits, List.of());
    }

    public PiProjectileTraceResult {
        Objects.requireNonNull(primaryHit, "primaryHit");
        entityHits = entityHits == null ? List.of() : List.copyOf(entityHits);
        grazeHits = grazeHits == null ? List.of() : List.copyOf(grazeHits);
    }

    public boolean hitEntity() {
        return primaryHit.getType() == HitResult.Type.ENTITY;
    }

    /**
     * Returns whether the trace collected at least one graze hit.
     *
     * @return {@code true} when a target passed through the graze radius without a real hit
     */
    public boolean hitGraze() {
        return !grazeHits.isEmpty();
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
