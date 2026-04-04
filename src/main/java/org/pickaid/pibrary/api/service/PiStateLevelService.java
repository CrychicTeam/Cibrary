package org.pickaid.pibrary.api.service;

import java.util.function.Consumer;
import org.pickaid.pibrary.runtime.state.PiHostState;
import org.pickaid.piserializekit.api.schema.PiDirtySet;

public abstract class PiStateLevelService<S> {
    private final PiHostState<S> host;

    protected PiStateLevelService(Class<S> stateType) {
        this.host = new PiHostState<>(stateType);
    }

    public final S viewState() {
        return host.viewState();
    }

    protected final void updateState(Consumer<S> change) {
        host.updateState(change);
    }

    protected final PiDirtySet dirtySet() {
        return host.dirtySet();
    }

    protected final void clearDirty() {
        host.clearDirty();
    }
}
