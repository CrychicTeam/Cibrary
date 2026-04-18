package org.pickaid.pibrary.dev.example;

import org.pickaid.pibrary.api.service.PiChunkService;
import org.pickaid.pibrary.api.service.PiChunkServiceContext;
import org.pickaid.pibrary.api.service.PiStateChunkService;

/**
 * Minimal sample chunk-scoped service backed by {@link CounterState}.
 */
@PiChunkService(namespace = "pibrary", path = "counter_chunk")
public final class CounterChunkService extends PiStateChunkService<CounterState> {
    /**
     * Creates the sample chunk service.
     *
     * @param context generated chunk service context
     */
    public CounterChunkService(PiChunkServiceContext context) {
        super(context);
    }

    /**
     * Increments the sample count.
     */
    public void increment() {
        updateState(state -> state.count++);
    }
}
