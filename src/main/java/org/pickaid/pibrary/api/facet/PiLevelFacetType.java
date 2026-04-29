package org.pickaid.pibrary.api.facet;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public interface PiLevelFacetType<T extends PiStateLevelFacet<?>> {
    ResourceLocation id();

    Class<T> facetClass();

    boolean isRegistered();

    Optional<T> find(ServerLevel level);

    default T get(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        return find(level).orElseThrow(() ->
                new IllegalStateException("Missing Pi level facet " + facetClass().getName() + " on " + level.dimension().location()));
    }
}
