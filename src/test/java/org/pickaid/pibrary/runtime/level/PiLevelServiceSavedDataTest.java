package org.pickaid.pibrary.runtime.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.core.PibraryServices;
import org.pickaid.pibrary.api.service.PiLevelServiceContext;
import org.pickaid.pibrary.dev.example.CounterLevelService;

class PiLevelServiceSavedDataTest {
    @Test
    void savesAndLoadsLevelServiceThroughPersistedProjection() {
        var descriptor = PiLevelServiceDescriptors.requireGenerated(CounterLevelService.class);
        PiLevelServiceSavedData<CounterLevelService> source =
                new PiLevelServiceSavedData<>(descriptor, new PiLevelServiceContext(null, PibraryServices.create()));
        source.service().increment();
        source.service().increment();
        source.service().increment();
        source.service().increment();
        source.service().gainEnergy(7);
        source.service().startTrial(ResourceLocation.fromNamespaceAndPath("pibrary", "trial"));
        source.service().setSessionGlow(11);

        CompoundTag persisted = source.save(new CompoundTag());

        assertTrue(persisted.contains("count"));
        assertTrue(persisted.contains("energy"));
        assertTrue(persisted.contains("trial"));
        assertFalse(persisted.contains("session_glow"));

        PiLevelServiceSavedData<CounterLevelService> restored =
                new PiLevelServiceSavedData<>(descriptor, new PiLevelServiceContext(null, PibraryServices.create()), persisted);

        assertEquals(4, restored.service().count());
        assertEquals(7, restored.service().energy());
        assertEquals(0, restored.service().sessionGlow());
        assertEquals(ResourceLocation.fromNamespaceAndPath("pibrary", "trial"), restored.service().trial());
    }

    @Test
    void storageIdUsesFilenameSafeStableForm() {
        assertEquals("pibrary__counter_level", PiLevelServiceDescriptors.requireGenerated(CounterLevelService.class).storageId());
    }

    @Test
    void savedDataTracksPersistedChangesOnly() {
        var descriptor = PiLevelServiceDescriptors.requireGenerated(CounterLevelService.class);
        PiLevelServiceSavedData<CounterLevelService> data =
                new PiLevelServiceSavedData<>(descriptor, new PiLevelServiceContext(null, PibraryServices.create()));

        assertFalse(data.isDirty());

        data.service().setSessionGlow(7);
        assertFalse(data.isDirty());

        data.service().increment();
        assertTrue(data.isDirty());

        data.save(new CompoundTag());
        assertFalse(data.isDirty());
    }

    @Test
    void saveReplacesStaleKeysInsteadOfLeavingResidualPayload() {
        var descriptor = PiLevelServiceDescriptors.requireGenerated(CounterLevelService.class);
        PiLevelServiceSavedData<CounterLevelService> data =
                new PiLevelServiceSavedData<>(descriptor, new PiLevelServiceContext(null, PibraryServices.create()));
        data.service().increment();

        CompoundTag existing = new CompoundTag();
        existing.putInt("legacy_only", 99);
        existing.putInt("session_glow", 7);

        CompoundTag saved = data.save(existing);

        assertFalse(saved.contains("legacy_only"));
        assertFalse(saved.contains("session_glow"));
        assertTrue(saved.contains("count"));
    }
}
