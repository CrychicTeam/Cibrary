package org.pickaid.pibrary.dev.example;

import org.pickaid.pibrary.api.facet.PiChunkFacetType;
import org.pickaid.pibrary.api.facet.PiChunkFacets;

public final class CounterChunkFacets {
    public static final PiChunkFacetType<CounterChunkFacet> COUNTER_CHUNK =
            PiChunkFacets.bind(CounterChunkFacet.class).register();

    private CounterChunkFacets() {
    }

    public static void register() {
    }
}
