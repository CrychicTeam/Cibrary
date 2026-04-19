package org.pickaid.pibrary.api.registrate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PiRegistrateTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        try {
            Bootstrap.bootStrap();
        } catch (ExceptionInInitializerError exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException
                    && runtimeException.getMessage() != null
                    && runtimeException.getMessage().contains("Error computing listener list for net.minecraftforge.network.NetworkEvent")) {
                return;
            }
            throw exception;
        }
    }

    @Test
    void createBuildsStableContextAndIds() {
        PiRegistrate registrate = PiRegistrate.create("pickaid");

        assertEquals("pickaid", registrate.getModid());
        assertEquals("pickaid:test_path", registrate.id("test_path").toString());
        assertSame(registrate.context(), registrate.context());
        assertEquals("pickaid", registrate.context().modId());
    }

    @Test
    void defaultsRejectBlankPath() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> PiRegistrateDefaults.requirePath("  "));

        assertEquals("path must not be blank", exception.getMessage());
    }

    @Test
    void rootExposesTypedBuilderEntryPoints() {
        PiRegistrate registrate = PiRegistrate.create("pickaid");

        assertNotNull(registrate.customRegistry("spell_type", String.class));
        assertNotNull(registrate.datapackRegistry("spell_preset", null, null));
        assertNotNull(registrate.levelService("weather", Object.class, context -> null));
    }
}
