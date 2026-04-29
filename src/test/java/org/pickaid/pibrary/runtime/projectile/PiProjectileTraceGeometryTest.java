package org.pickaid.pibrary.runtime.projectile;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class PiProjectileTraceGeometryTest {
    @Test
    void movingTargetHitUsesIntermediateTargetBounds() {
        AABB targetBounds = new AABB(2.0D, -0.5D, 4.5D, 3.0D, 0.5D, 5.5D);
        Vec3 targetDelta = new Vec3(-2.0D, 0.0D, 0.0D);
        Vec3 from = new Vec3(0.0D, 0.0D, 5.0D);
        Vec3 to = new Vec3(0.0D, 0.0D, 7.0D);

        assertTrue(PiDefaultProjectileService.traceMovingBounds(
                targetBounds,
                targetDelta,
                from,
                to,
                0.0D
        ).isPresent());
    }

    @Test
    void grazeInflationDoesNotRequireExactIntersection() {
        AABB targetBounds = new AABB(0.7D, -0.5D, 4.5D, 1.3D, 0.5D, 5.5D);
        Vec3 from = new Vec3(0.0D, 0.0D, 5.0D);
        Vec3 to = new Vec3(0.0D, 0.0D, 7.0D);

        assertTrue(PiDefaultProjectileService.traceMovingBounds(
                targetBounds,
                Vec3.ZERO,
                from,
                to,
                0.75D
        ).isPresent());
    }
}
