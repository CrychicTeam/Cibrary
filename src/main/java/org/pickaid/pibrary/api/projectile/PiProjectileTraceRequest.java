package org.pickaid.pibrary.api.projectile;

import java.util.Objects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.pickaid.pibrary.api.targeting.PiTargetQuery;

public record PiProjectileTraceRequest(
        Level level,
        Entity source,
        Vec3 from,
        Vec3 to,
        AABB sweepBounds,
        PiTargetQuery targetQuery,
        PiProjectileCollisionMode collisionMode,
        int maxEntityHits
) {
    public PiProjectileTraceRequest {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(sweepBounds, "sweepBounds");
        Objects.requireNonNull(targetQuery, "targetQuery");
        Objects.requireNonNull(collisionMode, "collisionMode");
        if (maxEntityHits < 1) {
            throw new IllegalArgumentException("maxEntityHits must be >= 1");
        }
    }
}
