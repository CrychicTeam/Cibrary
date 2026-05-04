package org.pickaid.pibrary.runtime.facet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.core.PibraryScopes;
import org.pickaid.pibrary.api.facet.PiLevelFacetContext;
import org.pickaid.pibrary.dev.example.CounterLevelFacet;

class PiLevelFacetSavedDataTest {
    @Test
    void savesAndLoadsLevelFacetThroughPersistedProjection() {
        var descriptor = PiLevelFacetDescriptors.requireGenerated(CounterLevelFacet.class);
        PiLevelFacetSavedData<CounterLevelFacet> source =
                new PiLevelFacetSavedData<>(descriptor, new PiLevelFacetContext(null, PibraryScopes.create()));
        source.facet().increment();
        source.facet().increment();
        source.facet().increment();
        source.facet().increment();
        source.facet().gainEnergy(7);
        source.facet().startTrial(new ResourceLocation("pibrary", "trial"));
        source.facet().setSessionGlow(11);

        CompoundTag persisted = source.save(new CompoundTag());

        assertTrue(persisted.contains("count"));
        assertTrue(persisted.contains("energy"));
        assertTrue(persisted.contains("trial"));
        assertFalse(persisted.contains("session_glow"));

        PiLevelFacetSavedData<CounterLevelFacet> restored =
                new PiLevelFacetSavedData<>(descriptor, new PiLevelFacetContext(null, PibraryScopes.create()), persisted);

        assertEquals(4, restored.facet().count());
        assertEquals(7, restored.facet().energy());
        assertEquals(0, restored.facet().sessionGlow());
        assertEquals(new ResourceLocation("pibrary", "trial"), restored.facet().trial());
    }

    @Test
    void storageIdUsesFilenameSafeStableForm() {
        assertEquals("pibrary__counter_level", PiLevelFacetDescriptors.requireGenerated(CounterLevelFacet.class).storageId());
    }

    @Test
    void savedDataTracksPersistedChangesOnly() {
        var descriptor = PiLevelFacetDescriptors.requireGenerated(CounterLevelFacet.class);
        PiLevelFacetSavedData<CounterLevelFacet> data =
                new PiLevelFacetSavedData<>(descriptor, new PiLevelFacetContext(null, PibraryScopes.create()));

        assertFalse(data.isDirty());

        data.facet().setSessionGlow(7);
        assertFalse(data.isDirty());

        data.facet().increment();
        assertTrue(data.isDirty());

        data.save(new CompoundTag());
        assertFalse(data.isDirty());
    }

    @Test
    void saveReplacesStaleKeysInsteadOfLeavingResidualPayload() {
        var descriptor = PiLevelFacetDescriptors.requireGenerated(CounterLevelFacet.class);
        PiLevelFacetSavedData<CounterLevelFacet> data =
                new PiLevelFacetSavedData<>(descriptor, new PiLevelFacetContext(null, PibraryScopes.create()));
        data.facet().increment();

        CompoundTag existing = new CompoundTag();
        existing.putInt("legacy_only", 99);
        existing.putInt("session_glow", 7);

        CompoundTag saved = data.save(existing);

        assertFalse(saved.contains("legacy_only"));
        assertFalse(saved.contains("session_glow"));
        assertTrue(saved.contains("count"));
    }
}
