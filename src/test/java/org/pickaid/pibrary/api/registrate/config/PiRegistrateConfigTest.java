package org.pickaid.pibrary.api.registrate.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mojang.serialization.Codec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.config.PiConfigEntry;
import org.pickaid.pibrary.api.config.PiConfigScope;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.api.registrate.PiRegistrateTestSupport;
import org.pickaid.pibrary.runtime.registrate.config.PiConfigRegistry;
import org.pickaid.pibrary.runtime.registrate.config.PiCreativeTabRegistry;

class PiRegistrateConfigTest {
    @AfterEach
    void clearRegistries() {
        PiConfigRegistry.clearForTests();
        PiCreativeTabRegistry.clearForTests();
    }

    @Test
    void configRegistrationCreatesTypedEntry() {
        PiRegistrate registrate = PiRegistrateTestSupport.create("pibrary");

        PiConfigEntry<Integer> entry = registrate.config("combat", Codec.INT, 5)
                .scope(PiConfigScope.SERVER_DATA_PACK)
                .register();

        assertEquals("pibrary:combat", entry.id().toString());
        assertEquals(PiConfigScope.SERVER_DATA_PACK, entry.scope());
        assertSame(entry, PiConfigRegistry.require(entry.id()));
    }

    @Test
    void duplicateConfigRegistrationIsRejected() {
        PiRegistrate registrate = PiRegistrateTestSupport.create("pibrary");
        registrate.config("combat", Codec.INT, 5).register();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> registrate.config("combat", Codec.INT, 9).register());

        assertEquals("Duplicate config entry: pibrary:combat", exception.getMessage());
    }

    @Test
    void creativeTabRegistrationCapturesTitleKey() {
        PiRegistrate registrate = PiRegistrateTestSupport.create("pibrary");

        PiCreativeTabRegistration entry = registrate.creativeTab("main", "PickAID").register();

        assertEquals("pibrary:main", entry.id().toString());
        assertEquals("PickAID", entry.title());
        assertEquals("itemGroup.pibrary.main", entry.translationKey());
        assertSame(entry, PiCreativeTabRegistry.require(entry.id()));
    }

    @Test
    void duplicateCreativeTabRegistrationIsRejected() {
        PiRegistrate registrate = PiRegistrateTestSupport.create("pibrary");
        registrate.creativeTab("main", "PickAID").register();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> registrate.creativeTab("main", "Duplicate").register());

        assertEquals("Duplicate creative tab: pibrary:main", exception.getMessage());
    }
}
