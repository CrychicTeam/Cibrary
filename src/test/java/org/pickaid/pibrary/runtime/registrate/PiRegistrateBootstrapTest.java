package org.pickaid.pibrary.runtime.registrate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.api.registrate.PiRegistrateTestSupport;

class PiRegistrateBootstrapTest {
    @Test
    void bootstrapIsIdempotent() {
        assertDoesNotThrow(PiRegistrateBootstrap::bootstrap);
        assertDoesNotThrow(PiRegistrateBootstrap::bootstrap);
        assertTrue(PiRegistrateBootstrap.isBootstrapped());
    }

    @Test
    void rootSeamCanBeCreatedAfterBootstrap() {
        PiRegistrateBootstrap.bootstrap();

        PiRegistrate registrate = PiRegistrateTestSupport.create("pickaid");

        assertEquals("pickaid:ready", registrate.id("ready").toString());
    }
}
