package org.pickaid.pibrary.runtime.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import net.minecraft.nbt.CompoundTag;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.pibrary.dev.example.CounterState_PiFields;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

class PiFacetStateTest {
    @Test
    void updateStateMarksOnlyChangedFields() {
        PiFacetState<CounterState> facetState = new PiFacetState<>(CounterState.class);

        facetState.updateState(state -> state.count++);

        assertEquals(Set.of(CounterState_PiFields.COUNT), facetState.dirtySet().keys());
    }

    @Test
    void updateStateDoesNotMarkFieldsWhenValuesStayEqual() {
        PiFacetState<CounterState> facetState = new PiFacetState<>(CounterState.class);

        facetState.updateState(state -> state.count = 0);

        assertTrue(facetState.dirtySet().keys().isEmpty());
    }

    @Test
    void clearDirtyRemovesMarkedFields() {
        PiFacetState<CounterState> facetState = new PiFacetState<>(CounterState.class);
        facetState.updateState(state -> state.count++);

        facetState.clearDirty();

        assertTrue(facetState.dirtySet().keys().isEmpty());
    }

    @Test
    void persistedSaveAndLoadIgnoreNonPersistentFields() {
        PiFacetState<CounterState> source = new PiFacetState<>(CounterState.class);
        source.updateState(state -> {
            state.count = 4;
            state.sessionGlow = 9;
            state.energy = 12;
        });

        CompoundTag persisted = source.savePersisted();

        assertTrue(persisted.contains("count"));
        assertTrue(persisted.contains("energy"));
        assertFalse(persisted.contains("session_glow"));

        PiFacetState<CounterState> restored = new PiFacetState<>(CounterState.class);
        restored.viewState().sessionGlow = 77;

        restored.loadPersisted(persisted, PiDecodeContext.strict());

        assertEquals(4, restored.viewState().count);
        assertEquals(12, restored.viewState().energy);
        assertEquals(77, restored.viewState().sessionGlow);
    }
}
