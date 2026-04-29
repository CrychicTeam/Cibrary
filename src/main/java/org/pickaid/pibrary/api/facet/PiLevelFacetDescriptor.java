package org.pickaid.pibrary.api.facet;

import net.minecraft.resources.ResourceLocation;

public interface PiLevelFacetDescriptor<T extends PiStateLevelFacet<S>, S> {
    ResourceLocation id();

    Class<T> facetClass();

    Class<S> stateType();

    String storageId();

    T create(PiLevelFacetContext context);
}
