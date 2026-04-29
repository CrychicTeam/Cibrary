package org.pickaid.pibrary.api.targeting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class PiTargetQueryTest {
    @Test
    void projectedPointNormalizesDirectionAndSupportsFlagToggles() {
        PiTargetQuery query = PiTargetQuery.projectedPoint(new Vec3(1.0D, 2.0D, 3.0D), new Vec3(0.0D, 0.0D, 3.0D), 8.0D, 1.5D)
                .requiringLineOfSight()
                .livingTargetsOnly()
                .includingCaster();

        assertEquals(PiTargetAnchor.PROJECTED_POINT, query.anchor());
        assertEquals(8.0D, query.range());
        assertEquals(1.5D, query.radius());
        assertEquals(new Vec3(1.0D, 2.0D, 3.0D), query.anchorPoint());
        assertEquals(new Vec3(0.0D, 0.0D, 1.0D), query.anchorDirection());
        assertTrue(query.requireLineOfSight());
        assertTrue(query.livingOnly());
        assertTrue(query.includeCaster());
    }

    @Test
    void areaCenterRequiresAnchorPointAndRejectsDirection() {
        assertThrows(NullPointerException.class, () ->
                new PiTargetQuery(PiTargetAnchor.AREA_CENTER, 0.0D, 2.0D, false, false, false, null, null));
        assertThrows(IllegalArgumentException.class, () ->
                new PiTargetQuery(PiTargetAnchor.AREA_CENTER, 0.0D, 2.0D, false, false, false, Vec3.ZERO, new Vec3(1.0D, 0.0D, 0.0D)));
    }

    @Test
    void selfFactoryStartsIsolatedWithoutOptionalFlags() {
        PiTargetQuery query = PiTargetQuery.self(3.0D);

        assertEquals(PiTargetAnchor.SELF, query.anchor());
        assertEquals(0.0D, query.range());
        assertEquals(3.0D, query.radius());
        assertFalse(query.requireLineOfSight());
        assertFalse(query.livingOnly());
        assertFalse(query.includeCaster());
    }
}
