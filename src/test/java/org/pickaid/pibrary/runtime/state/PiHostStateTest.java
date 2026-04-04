package org.pickaid.pibrary.runtime.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.pibrary.dev.example.CounterState_PiFields;

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
}
