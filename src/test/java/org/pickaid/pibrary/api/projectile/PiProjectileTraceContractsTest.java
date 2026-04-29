package org.pickaid.pibrary.api.projectile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class PiProjectileTraceContractsTest {
    @Test
    void legacyTraceRequestConstructorDefaultsGrazeRadiusToZero() {
        assertThrows(NullPointerException.class, () -> new PiProjectileTraceRequest(
                null,
                null,
                Vec3.ZERO,
                Vec3.ZERO,
                new net.minecraft.world.phys.AABB(Vec3.ZERO, Vec3.ZERO),
                org.pickaid.pibrary.api.targeting.PiTargetQuery.self(1.0D),
                PiProjectileCollisionMode.STOP_ON_ANY_HIT,
                1
        ));

        assertThrows(IllegalArgumentException.class, () -> new PiProjectileTraceRequest(
                null,
                null,
                Vec3.ZERO,
                Vec3.ZERO,
                new net.minecraft.world.phys.AABB(Vec3.ZERO, Vec3.ZERO),
                org.pickaid.pibrary.api.targeting.PiTargetQuery.self(1.0D),
                PiProjectileCollisionMode.STOP_ON_ANY_HIT,
                1,
                -0.01D
        ));
    }

    @Test
    void traceResultCopiesGrazeHitsAndReportsGrazeState() {
        BlockHitResult miss = BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO);
        EntityHitResult graze = new EntityHitResult(null, Vec3.ZERO);
        List<EntityHitResult> grazes = new ArrayList<>();
        grazes.add(graze);

        PiProjectileTraceResult result = new PiProjectileTraceResult(miss, List.of(), grazes);
        grazes.clear();

        assertFalse(result.hitEntity());
        assertTrue(result.hitGraze());
        assertEquals(1, result.grazeHits().size());
        assertThrows(UnsupportedOperationException.class, () -> result.grazeHits().clear());
    }
}
