package org.pickaid.pibrary.dev.example;

import org.pickaid.pibrary.api.facet.PiLivingFacetType;
import org.pickaid.pibrary.api.facet.PiLivingFacets;

public final class CounterFacets {
    public static final PiLivingFacetType<CounterPlayerFacet> COUNTER_PLAYER =
            PiLivingFacets.bind(CounterPlayerFacet.class).register();

    private CounterFacets() {
    }

    public static void register() {
    }
}
