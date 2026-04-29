package org.pickaid.pibrary.dev.example;

import org.pickaid.pibrary.api.facet.PiLevelFacetType;
import org.pickaid.pibrary.api.facet.PiLevelFacets;

public final class CounterLevelFacets {
    public static final PiLevelFacetType<CounterLevelFacet> COUNTER_LEVEL =
            PiLevelFacets.bind(CounterLevelFacet.class).register();

    private CounterLevelFacets() {
    }

    public static void register() {
    }
}
