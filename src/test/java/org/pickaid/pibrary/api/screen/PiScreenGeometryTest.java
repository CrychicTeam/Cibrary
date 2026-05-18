package org.pickaid.pibrary.api.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PiScreenGeometryTest {
    @Test
    void rectContainsPointAndConvertsToLocal() {
        PiScreenRect rect = new PiScreenRect(10, 20, 100, 50);
        PiScreenPoint point = PiScreenPoint.screen(25, 35);

        assertTrue(rect.contains(point));
        assertEquals(PiScreenPoint.local(15, 15), rect.toLocal(point));
    }
}
