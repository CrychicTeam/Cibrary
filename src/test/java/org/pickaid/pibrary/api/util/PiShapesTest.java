package org.pickaid.pibrary.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

class PiShapesTest {
    @Test
    void box16UsesModelCoordinates() {
        VoxelShape shape = PiShapes.box16(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);

        assertEquals(new AABB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D), shape.bounds());
        assertThrows(IllegalArgumentException.class,
                () -> PiShapes.box16(0.0D, 0.0D, 0.0D, 17.0D, 8.0D, 16.0D));
    }

    @Test
    void commonLocalShapesCoverBlockDevelopmentCases() {
        assertEquals(new AABB(0.0D, 0.875D, 0.0D, 1.0D, 1.0D, 1.0D),
                PiShapes.face16(Direction.UP, 2.0D).bounds());
        assertEquals(new AABB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D),
                PiShapes.centerColumn16(4.0D, 0.0D, 16.0D).bounds());
    }

    @Test
    void combinesAabbBackedShapes() {
        VoxelShape shape = PiShapes.fromAabbs(
                new AABB(0.0D, 0.0D, 0.0D, 0.5D, 0.5D, 0.5D),
                new AABB(0.5D, 0.5D, 0.5D, 1.0D, 1.0D, 1.0D)
        );

        assertEquals(new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), shape.bounds());
    }
}
