package org.pickaid.pibrary.api.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class PiAabbsTest {
    @Test
    void faceAndEdgesUseBlockLocalCoordinates() {
        assertEquals(new AABB(0.0D, 0.875D, 0.0D, 1.0D, 1.0D, 1.0D), PiAabbs.face(Direction.UP, 0.125D));
        assertEquals(12, PiAabbs.unitEdges(0.125D).size());
        assertThrows(IllegalArgumentException.class, () -> PiAabbs.face(Direction.UP, 1.0D));
    }

    @Test
    void includeBlockUsesFullBlockVolume() {
        AABB box = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

        assertEquals(new AABB(0.0D, 0.0D, 0.0D, 3.0D, 4.0D, 5.0D), PiAabbs.include(box, new BlockPos(2, 3, 4)));
    }

    @Test
    void touchedBlocksTreatMaxBoundaryAsExclusive() {
        List<BlockPos> positions = PiAabbs.touchedBlocks(new AABB(0.0D, 0.0D, 0.0D, 2.0D, 1.0D, 1.0D));

        assertEquals(List.of(new BlockPos(0, 0, 0), new BlockPos(1, 0, 0)), positions);
    }

    @Test
    void roundApproximationCanBeUsedAsBroadPhase() {
        List<AABB> sphere = PiAabbs.approximateSphere(Vec3.ZERO, 2.0D);

        assertEquals(7, sphere.size());
        assertTrue(PiAabbs.intersectsAny(new AABB(-0.1D, -0.1D, -0.1D, 0.1D, 0.1D, 0.1D), sphere));
        assertFalse(PiAabbs.intersectsAny(new AABB(5.0D, 5.0D, 5.0D, 6.0D, 6.0D, 6.0D), sphere));
    }
}
