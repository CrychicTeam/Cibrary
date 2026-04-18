package org.pickaid.pibrary.runtime.targeting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.targeting.PiTargetAnchor;
import org.pickaid.pibrary.api.targeting.PiTargetQuery;

class PiDefaultTargetingServiceTest {
    @Test
    void resolveVolumeBuildsLookSweepFromCasterEyeAndView() {
        PiDefaultTargetingService.ResolvedTargetVolume volume = PiDefaultTargetingService.resolveVolume(
                new Vec3(0.0D, 0.0D, 0.0D),
                new Vec3(0.0D, 1.6D, 0.0D),
                new Vec3(0.0D, 0.0D, 1.0D),
                PiTargetQuery.look(10.0D, 0.5D)
        );

        assertEquals(PiTargetAnchor.LOOK_VECTOR, volume.anchor());
        assertEquals(new Vec3(0.0D, 1.6D, 0.0D), volume.start());
        assertEquals(new Vec3(0.0D, 1.6D, 10.0D), volume.end());
        assertEquals(new AABB(new Vec3(0.0D, 1.6D, 0.0D), new Vec3(0.0D, 1.6D, 10.0D)).inflate(0.5D), volume.queryBounds());
    }

    @Test
    void resolveVolumeBuildsProjectedPointSphereFromPointAndDirection() {
        PiDefaultTargetingService.ResolvedTargetVolume volume = PiDefaultTargetingService.resolveVolume(
                Vec3.ZERO,
                new Vec3(0.0D, 1.6D, 0.0D),
                new Vec3(1.0D, 0.0D, 0.0D),
                PiTargetQuery.projectedPoint(new Vec3(2.0D, 3.0D, 4.0D), new Vec3(0.0D, 2.0D, 0.0D), 5.0D, 1.25D)
        );

        assertEquals(PiTargetAnchor.PROJECTED_POINT, volume.anchor());
        assertEquals(new Vec3(2.0D, 8.0D, 4.0D), volume.center());
        assertEquals(new AABB(new Vec3(2.0D, 8.0D, 4.0D), new Vec3(2.0D, 8.0D, 4.0D)).inflate(1.25D), volume.queryBounds());
    }

    @Test
    void matchesUsesSweepInflationForLookQueries() {
        PiDefaultTargetingService.ResolvedTargetVolume volume = PiDefaultTargetingService.resolveVolume(
                Vec3.ZERO,
                new Vec3(0.0D, 1.6D, 0.0D),
                new Vec3(0.0D, 0.0D, 1.0D),
                PiTargetQuery.look(8.0D, 0.6D)
        );

        assertTrue(PiDefaultTargetingService.matches(new AABB(-0.2D, 1.2D, 3.8D, 0.2D, 2.0D, 4.2D), volume));
        assertFalse(PiDefaultTargetingService.matches(new AABB(2.0D, 1.0D, 4.0D, 3.0D, 2.0D, 5.0D), volume));
    }

    @Test
    void matchesUsesSphereDistanceForAreaQueries() {
        PiDefaultTargetingService.ResolvedTargetVolume volume = PiDefaultTargetingService.resolveVolume(
                Vec3.ZERO,
                new Vec3(0.0D, 1.6D, 0.0D),
                new Vec3(0.0D, 0.0D, 1.0D),
                PiTargetQuery.areaCenter(new Vec3(4.0D, 0.0D, 4.0D), 2.0D)
        );

        assertTrue(PiDefaultTargetingService.matches(new AABB(5.0D, -0.5D, 4.0D, 5.6D, 0.5D, 4.6D), volume));
        assertFalse(PiDefaultTargetingService.matches(new AABB(7.5D, -0.5D, 4.0D, 8.0D, 0.5D, 4.5D), volume));
    }
}
