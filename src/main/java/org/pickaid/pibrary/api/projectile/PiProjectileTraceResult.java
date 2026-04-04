package org.pickaid.pibrary.api.projectile;

import java.util.List;
import java.util.Objects;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

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

    public boolean hitBlock() {
        return primaryHit.getType() == HitResult.Type.BLOCK;
    }
}
