package org.pickaid.pibrary.runtime.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.core.PibraryScope;
import org.pickaid.pibrary.api.core.PibraryScopes;
import org.pickaid.pibrary.api.facet.PiLivingFacetContext;
import org.pickaid.pibrary.api.facet.PiLivingFacetContainer;
import org.pickaid.pibrary.dev.example.CounterPlayerFacet;
import org.pickaid.pibrary.dev.example.CounterState_PiFields;
import org.pickaid.pibrary.dev.example.CounterState_PiSchema;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import org.pickaid.piserializekit.api.schema.PiDirtySet;
import org.pickaid.piserializekit.api.schema.PiFieldKey;
import org.pickaid.piserializekit.api.schema.PiSyncScope;

class PiSyncFlushPlanTest {
    private static final PibraryScope DETACHED_SCOPE = PibraryScopes.create();

    private static final PiLivingFacetContainer DETACHED_CONTAINER = new PiLivingFacetContainer() {
        @Override
        public net.minecraft.world.entity.LivingEntity living() {
            return null;
        }

        @Override
        public PibraryScope scope() {
            return DETACHED_SCOPE;
        }

        @Override
        public <T extends org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet<?>> T get(Class<T> facetClass) {
            throw new UnsupportedOperationException("Detached container does not resolve facets");
        }
    };

    private static CounterPlayerFacet newFacet() {
        return new CounterPlayerFacet(new PiLivingFacetContext(null, DETACHED_CONTAINER));
    }

    @Test
    void groupsDirtyKeysBySyncScope() {
        PiFieldKey players = new PiFieldKey(0, "players");
        PiFieldKey ownerEnergy = new PiFieldKey(1, "owner_energy");

        PiSyncFlushPlan plan = new PiSyncFlushPlan()
                .add(players, PiSyncScope.TRACKING)
                .add(ownerEnergy, PiSyncScope.OWNER);

        assertEquals(List.of(players), plan.keys(PiSyncScope.TRACKING));
        assertEquals(List.of(ownerEnergy), plan.keys(PiSyncScope.OWNER));
    }

    @Test
    void facetsCanMarkDirtyFieldsWithoutManualPacketCode() {
        CounterPlayerFacet facet = newFacet();
        facet.gainEnergy(1);

        assertEquals(1, facet.energy());
        assertEquals(1, facet.dirtyCount());
    }

    @Test
    void generatedFieldConstantsAreAvailableInConsumerRepo() {
        assertEquals("count", CounterState_PiFields.COUNT.id());
        assertEquals(0, CounterState_PiFields.COUNT.index());
        assertEquals("energy", CounterState_PiFields.ENERGY.id());
        assertEquals(1, CounterState_PiFields.ENERGY.index());
    }

    @Test
    void generatedSchemaDescriptorsCanDriveFlushPlanning() {
        PiSyncFlushPlan plan = new PiSyncFlushPlan()
                .add(CounterState_PiSchema.COUNT)
                .add(CounterState_PiSchema.ENERGY);

        assertEquals(List.of(CounterState_PiFields.COUNT), plan.keys(PiSyncScope.CHUNK));
        assertEquals(List.of(CounterState_PiFields.ENERGY), plan.keys(PiSyncScope.OWNER));
    }

    @Test
    void generatedSchemaCanRoundTripStateAndDelta() {
        CounterState state = new CounterState();
        state.count = 3;
        state.energy = 7;

        CompoundTag full = CounterState_PiSchema.saveFull(state);
        assertEquals(CounterState_PiSchema.SCHEMA_ID, full.getString("__pi_schema"));
        assertEquals(CounterState_PiSchema.VERSION, full.getInt("__pi_version"));
        CounterState restored = new CounterState();
        CounterState_PiSchema.loadFull(restored, full, PiDecodeContext.strict());

        assertEquals(3, restored.count);
        assertEquals(7, restored.energy);

        CompoundTag delta = CounterState_PiSchema.writeDelta(state, new PiDirtySet().mark(CounterState_PiFields.ENERGY));
        assertEquals(CounterState_PiSchema.SCHEMA_ID, delta.getString("__pi_schema"));
        assertEquals(CounterState_PiSchema.VERSION, delta.getInt("__pi_version"));
        assertTrue(delta.contains("energy"));
        assertTrue(!delta.contains("count"));

        CounterState deltaState = new CounterState();
        deltaState.count = 99;
        CounterState_PiSchema.applyDelta(deltaState, delta, PiDecodeContext.strict());
        assertEquals(99, deltaState.count);
        assertEquals(7, deltaState.energy);
    }

    @Test
    void generatedSchemaRejectsWrongHeaderBeforeMutatingState() {
        CounterState deltaState = new CounterState();
        deltaState.count = 4;
        deltaState.energy = 8;

        CompoundTag delta = CounterState_PiSchema.writeDelta(new CounterState(), new PiDirtySet().mark(CounterState_PiFields.ENERGY));
        delta.putString("__pi_schema", "wrong.Schema");

        PiDecodeContext context = PiDecodeContext.strict();
        CounterState_PiSchema.applyDelta(deltaState, delta, context);

        assertEquals(4, deltaState.count);
        assertEquals(8, deltaState.energy);
        assertTrue(context.result().hasFatal());
        assertEquals("__pi_schema", context.result().issues().get(0).path());
    }

    @Test
    void consumerSchemaSupportsBooleanStringUuidAndResourceLocationFields() {
        CounterState state = new CounterState();
        state.active = false;
        state.ownerName = "alice";
        state.runId = UUID.fromString("123e4567-e89b-12d3-a456-426614174010");
        state.trial = new ResourceLocation("pibrary:trial");

        CompoundTag full = CounterState_PiSchema.saveFull(state);
        CounterState restored = new CounterState();
        restored.active = true;
        restored.ownerName = "fallback";
        restored.runId = UUID.fromString("123e4567-e89b-12d3-a456-426614174011");
        restored.trial = new ResourceLocation("pibrary:fallback");
        CounterState_PiSchema.loadFull(restored, full, PiDecodeContext.strict());

        assertFalse(restored.active);
        assertEquals("alice", restored.ownerName);
        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174010"), restored.runId);
        assertEquals(new ResourceLocation("pibrary:trial"), restored.trial);

        CompoundTag delta = CounterState_PiSchema.writeDelta(
                state,
                new PiDirtySet()
                        .mark(CounterState_PiFields.ACTIVE)
                        .mark(CounterState_PiFields.RUN_ID)
                        .mark(CounterState_PiFields.TRIAL)
        );
        assertTrue(delta.contains("active"));
        assertTrue(delta.contains("run_id"));
        assertTrue(delta.contains("trial"));
        assertFalse(delta.contains("owner_name"));
    }
}
