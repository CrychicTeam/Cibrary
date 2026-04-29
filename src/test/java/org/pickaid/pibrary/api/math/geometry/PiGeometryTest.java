package org.pickaid.pibrary.api.math.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class PiGeometryTest {
    @Test
    void vectorsNormalizeAndProjectPredictably() {
        assertEquals(new Vec3(0.0D, 0.0D, 1.0D), PiVectors.normalizeOrZero(new Vec3(0.0D, 0.0D, 4.0D)));
        assertSame(Vec3.ZERO, PiVectors.normalizeOrZero(Vec3.ZERO));
        assertEquals(new Vec3(1.5D, 2.5D, 3.5D), PiVectors.centerOf(new BlockPos(1, 2, 3)));
        assertEquals(new Vec3(0.0D, 0.0D, 5.0D), PiVectors.project(Vec3.ZERO, new Vec3(0.0D, 0.0D, 1.0D), 5.0D));
        assertEquals(new Vec3(4.0D, 2.0D, 3.0D), PiVectors.project(new Vec3(1.0D, 2.0D, 3.0D), new Vec3(2.0D, 0.0D, 0.0D), 3.0D));
    }

    @Test
    void aabbsCreateCornersAndSegmentBounds() {
        AABB box = PiAabbs.around(new Vec3(1.0D, 2.0D, 3.0D), 0.5D);

        assertEquals(new AABB(0.5D, 1.5D, 2.5D, 1.5D, 2.5D, 3.5D), box);
        assertEquals(new AABB(1.0D, 2.0D, 3.0D, 2.0D, 3.0D, 4.0D), PiAabbs.block(new BlockPos(1, 2, 3)));
        assertEquals(new AABB(0.5D, 1.5D, 2.5D, 4.0D, 5.0D, 6.0D), PiAabbs.include(box, new Vec3(4.0D, 5.0D, 6.0D)));
        assertIterableEquals(List.of(
            new Vec3(0.5D, 1.5D, 2.5D),
            new Vec3(0.5D, 1.5D, 3.5D),
            new Vec3(0.5D, 2.5D, 2.5D),
            new Vec3(0.5D, 2.5D, 3.5D),
            new Vec3(1.5D, 1.5D, 2.5D),
            new Vec3(1.5D, 1.5D, 3.5D),
            new Vec3(1.5D, 2.5D, 2.5D),
            new Vec3(1.5D, 2.5D, 3.5D)
        ), PiAabbs.corners(box));
        assertEquals(new AABB(0.0D, 0.0D, 0.0D, 4.0D, 1.0D, 1.0D),
            PiRays.segmentBounds(new Vec3(0.0D, 0.0D, 0.0D), new Vec3(4.0D, 1.0D, 1.0D), 0.0D));
        assertEquals(new AABB(-0.25D, -0.25D, -0.25D, 4.25D, 1.25D, 1.25D),
            PiRays.segmentBounds(new Vec3(0.0D, 0.0D, 0.0D), new Vec3(4.0D, 1.0D, 1.0D), 0.25D));
    }

    @Test
    void directionsProvidePlaneHelpers() {
        assertIterableEquals(List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST),
            PiDirections.horizontal());
        assertIterableEquals(List.of(Direction.DOWN, Direction.UP),
            PiDirections.vertical());
        assertIterableEquals(List.of(Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.DOWN),
            PiDirections.excludingAxis(Direction.Axis.X));
        assertIterableEquals(List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST),
            PiDirections.excludingAxis(Direction.Axis.Y));
        assertIterableEquals(List.of(Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN),
            PiDirections.excludingAxis(Direction.Axis.Z));
    }

    @Test
    void invalidGeometryInputsFailFast() {
        assertThrows(NullPointerException.class, () -> PiVectors.normalizeOrZero(null));
        assertThrows(NullPointerException.class, () -> PiVectors.centerOf(null));
        assertThrows(NullPointerException.class, () -> PiVectors.project(null, Vec3.ZERO, 1.0D));
        assertThrows(NullPointerException.class, () -> PiVectors.project(Vec3.ZERO, null, 1.0D));
        assertThrows(NullPointerException.class, () -> PiAabbs.around(null, 1.0D));
        assertThrows(NullPointerException.class, () -> PiAabbs.block(null));
        assertThrows(NullPointerException.class, () -> PiAabbs.include(null, Vec3.ZERO));
        assertThrows(NullPointerException.class, () -> PiAabbs.include(new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), null));
        assertThrows(IllegalArgumentException.class, () -> PiAabbs.around(Vec3.ZERO, -0.1D));
        assertThrows(IllegalArgumentException.class, () -> PiAabbs.around(Vec3.ZERO, Double.NaN));
        assertThrows(NullPointerException.class, () -> PiAabbs.corners(null));
        assertThrows(NullPointerException.class, () -> PiDirections.excludingAxis(null));
        assertThrows(NullPointerException.class, () -> PiRays.segmentBounds(null, Vec3.ZERO, 0.0D));
        assertThrows(NullPointerException.class, () -> PiRays.segmentBounds(Vec3.ZERO, null, 0.0D));
        assertThrows(IllegalArgumentException.class, () -> PiRays.segmentBounds(Vec3.ZERO, new Vec3(1.0D, 0.0D, 0.0D), -0.1D));
        assertThrows(IllegalArgumentException.class, () -> PiRays.segmentBounds(Vec3.ZERO, new Vec3(1.0D, 0.0D, 0.0D), Double.POSITIVE_INFINITY));
    }
}
