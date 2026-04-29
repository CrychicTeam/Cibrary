package org.pickaid.pibrary.api.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class PiOrientationTest {
    @Test
    void orientationBuildsRightHandedFrameFromForward() {
        PiOrientation orientation = PiOrientation.fromForward(new Vec3(0.0D, 0.0D, 2.0D));

        assertEquals(new Vec3(0.0D, 0.0D, 1.0D), orientation.forward());
        assertEquals(new Vec3(0.0D, 1.0D, 0.0D), orientation.up());
        assertEquals(new Vec3(1.0D, 0.0D, 0.0D), orientation.right());
        assertEquals(new Vec3(1.0D, 2.0D, 3.0D), orientation.offset(1.0D, 2.0D, 3.0D));
    }

    @Test
    void pitchRotatesForwardAndUpAroundRightAxis() {
        PiOrientation pitched = PiOrientation.fromForward(new Vec3(0.0D, 0.0D, 1.0D)).pitch(Math.PI * 0.5D);

        assertTrue(pitched.forward().distanceTo(new Vec3(0.0D, -1.0D, 0.0D)) < 1.0E-6D);
    }
}
