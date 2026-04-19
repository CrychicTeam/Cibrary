package org.pickaid.pibrary.api.registrate.host;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.api.registrate.PiRegistrateTestSupport;
import org.pickaid.pibrary.api.service.PiChunkServiceType;
import org.pickaid.pibrary.api.service.PiLevelServiceType;
import org.pickaid.pibrary.api.service.PiLivingServiceType;
import org.pickaid.pibrary.dev.example.CounterBlockEntity;
import org.pickaid.pibrary.dev.example.CounterChunkService;
import org.pickaid.pibrary.dev.example.CounterLevelService;
import org.pickaid.pibrary.dev.example.CounterPlayerService;
import org.pickaid.pibrary.dev.example.CounterState;

class PiRegistrateHostEntriesTest {
    @Test
    void entityAndPlayerEntriesUseSameLivingRuntimeHandle() {
        PiRegistrate registrate = PiRegistrateTestSupport.create("pibrary");

        PiEntityServiceEntry<CounterState, CounterPlayerService> entry =
                registrate.entityService("counter_player", CounterState.class, CounterPlayerService::new);
        PiLivingServiceType<CounterPlayerService> handle = entry.ownerSync().persisted().register(CounterPlayerService.class);

        assertSame(handle, entry.register(CounterPlayerService.class));
        assertEquals("owner", entry.syncMode().id());
    }

    @Test
    void chunkAndLevelEntriesExposeTypedHandles() {
        PiRegistrate registrate = PiRegistrateTestSupport.create("pibrary");

        PiChunkServiceType<CounterChunkService> chunkHandle =
                registrate.chunkService("counter_chunk", CounterState.class, CounterChunkService::new)
                        .trackingSync()
                        .persisted()
                        .register(CounterChunkService.class);

        PiLevelServiceType<CounterLevelService> levelHandle =
                registrate.levelService("counter_level", CounterState.class, CounterLevelService::new)
                        .noSyncByDefault()
                        .persisted()
                        .register(CounterLevelService.class);

        assertEquals(
                "tracking",
                ((PiChunkServiceEntry<?, ?>) registrate.chunkService("counter_chunk_again", CounterState.class, CounterChunkService::new))
                        .trackingSync()
                        .syncMode()
                        .id());
        assertSame(chunkHandle, org.pickaid.pibrary.api.service.PiChunkServices.type(CounterChunkService.class));
        assertSame(levelHandle, org.pickaid.pibrary.api.service.PiLevelServices.type(CounterLevelService.class));
    }

    @Test
    void blockEntityEntryKeepsTypedRegistrationMetadata() {
        PiRegistrate registrate = PiRegistrateTestSupport.create("pibrary");

        PiBlockEntityStateEntry<CounterState, CounterBlockEntity> entry =
                registrate.blockEntityService("counter_block", CounterState.class, CounterBlockEntity.class);

        assertSame(registrate, entry.owner());
        assertEquals("pibrary:counter_block", entry.id().toString());
        assertSame(CounterState.class, entry.stateType());
        assertSame(CounterBlockEntity.class, entry.blockEntityType());
    }

    @Test
    void entityEntryRejectsDescriptorIdMismatch() {
        PiRegistrate registrate = PiRegistrateTestSupport.create("pickaid");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registrate.entityService("counter_player", CounterState.class, CounterPlayerService::new)
                        .register(CounterPlayerService.class));

        assertEquals(
                "Pi registrate host entry id pickaid:counter_player does not match generated descriptor id pibrary:counter_player for "
                        + CounterPlayerService.class.getName(),
                exception.getMessage());
    }
}
