package org.pickaid.pibrary.api.facet;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.runtime.facet.PiActiveLevelFacetRegistry;

/**
 * Entry points for registering and resolving level-scoped facets.
 */
public final class PiLevelFacets {
    private PiLevelFacets() {
    }

    public static <T extends PiStateLevelFacet<?>> PiLevelFacetBinding<T> bind(Class<T> facetClass) {
        return new PiLevelFacetBinding<>(facetClass);
    }

    public static <T extends PiStateLevelFacet<?>> PiLevelFacetType<T> type(Class<T> facetClass) {
        return PiActiveLevelFacetRegistry.require(facetClass);
    }

    public static <T extends PiStateLevelFacet<?>> Optional<T> find(@Nullable ServerLevel level, Class<T> facetClass) {
        return level == null
                ? Optional.empty()
                : PiActiveLevelFacetRegistry.find(facetClass).flatMap(type -> type.find(level));
    }

    public static <T extends PiStateLevelFacet<?>> T require(ServerLevel level, Class<T> facetClass) {
        return type(facetClass).get(level);
    }
}
