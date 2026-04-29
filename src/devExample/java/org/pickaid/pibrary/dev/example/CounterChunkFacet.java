package org.pickaid.pibrary.dev.example;

import net.minecraft.world.level.chunk.LevelChunk;
import org.pickaid.pibrary.api.facet.PiChunkFacet;
import org.pickaid.pibrary.api.facet.PiChunkFacetContext;
import org.pickaid.pibrary.api.facet.PiStateChunkFacet;

@PiChunkFacet(namespace = "pibrary", path = "counter_chunk")
public final class CounterChunkFacet extends PiStateChunkFacet<CounterState> {
    public CounterChunkFacet(PiChunkFacetContext context) {
        super(context);
    }

    public void increment() {
        updateState(state -> state.count++);
    }

    public void gainEnergy(int value) {
        updateState(state -> state.energy += value);
    }

    public int count() {
        return viewState().count;
    }

    public int energy() {
        return viewState().energy;
    }

    public static CounterChunkFacet get(LevelChunk chunk) {
        return CounterChunkFacets.COUNTER_CHUNK.get(chunk);
    }
}
