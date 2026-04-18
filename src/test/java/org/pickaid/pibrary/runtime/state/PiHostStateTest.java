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

class PiHostStateTest {
    @Test
    void updateStateMarksOnlyChangedFields() {
        PiHostState<CounterState> host = new PiHostState<>(CounterState.class);

        host.updateState(state -> state.count++);

        assertEquals(Set.of(CounterState_PiFields.COUNT), host.dirtySet().keys());
    }

    @Test
    void updateStateDoesNotMarkFieldsWhenValuesStayEqual() {
        PiHostState<CounterState> host = new PiHostState<>(CounterState.class);

        host.updateState(state -> state.count = 0);

        assertTrue(host.dirtySet().keys().isEmpty());
    }

    @Test
    void clearDirtyRemovesMarkedFields() {
        PiHostState<CounterState> host = new PiHostState<>(CounterState.class);
        host.updateState(state -> state.count++);

        host.clearDirty();

        assertTrue(host.dirtySet().keys().isEmpty());
    }

    @Test
    void persistedSaveAndLoadIgnoreNonPersistentFields() {
        PiHostState<CounterState> source = new PiHostState<>(CounterState.class);
        source.updateState(state -> {
            state.count = 4;
            state.sessionGlow = 9;
            state.energy = 12;
        });

        CompoundTag persisted = source.savePersisted();

        assertTrue(persisted.contains("count"));
        assertTrue(persisted.contains("energy"));
        assertFalse(persisted.contains("session_glow"));

        PiHostState<CounterState> restored = new PiHostState<>(CounterState.class);
        restored.viewState().sessionGlow = 77;

        restored.loadPersisted(persisted, PiDecodeContext.strict());

        assertEquals(4, restored.viewState().count);
        assertEquals(12, restored.viewState().energy);
        assertEquals(77, restored.viewState().sessionGlow);
    }
}
