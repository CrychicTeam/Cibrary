package org.pickaid.pibrary.api.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class PiVectorsTest {
    @Test
    void safeNormalizeUsesFallbackForZeroVector() {
        assertEquals(new Vec3(0.0D, 1.0D, 0.0D), PiVectors.safeNormalize(Vec3.ZERO, new Vec3(0.0D, 2.0D, 0.0D)));
    }

    @Test
    void projectionAndRejectionSplitVector() {
        Vec3 source = new Vec3(2.0D, 3.0D, 0.0D);

        assertEquals(new Vec3(2.0D, 0.0D, 0.0D), PiVectors.projectOnto(source, new Vec3(1.0D, 0.0D, 0.0D)));
        assertEquals(new Vec3(0.0D, 3.0D, 0.0D), PiVectors.rejectFrom(source, new Vec3(1.0D, 0.0D, 0.0D)));
    }

    @Test
    void rotatesAroundAxis() {
        Vec3 rotated = PiVectors.rotateAroundAxis(
                new Vec3(1.0D, 0.0D, 0.0D),
                new Vec3(0.0D, 1.0D, 0.0D),
                Math.PI * 0.5D
        );

        assertTrue(rotated.distanceTo(new Vec3(0.0D, 0.0D, -1.0D)) < 1.0E-6D);
    }

    @Test
    void signedAngleUsesNormalForDirection() {
        double angle = PiVectors.signedAngleRadians(
                new Vec3(1.0D, 0.0D, 0.0D),
                new Vec3(0.0D, 0.0D, -1.0D),
                new Vec3(0.0D, 1.0D, 0.0D)
        );

        assertEquals(Math.PI * 0.5D, angle, 1.0E-6D);
    }
}
