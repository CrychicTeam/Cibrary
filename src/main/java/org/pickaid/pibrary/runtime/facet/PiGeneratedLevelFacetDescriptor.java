package org.pickaid.pibrary.runtime.facet;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.pickaid.pibrary.api.facet.PiLevelFacetDescriptor;
import org.pickaid.pibrary.api.facet.PiLevelFacetType;
import org.pickaid.pibrary.api.facet.PiLevelFacetStorages;
import org.pickaid.pibrary.api.facet.PiStateLevelFacet;

public abstract class PiGeneratedLevelFacetDescriptor<T extends PiStateLevelFacet<S>, S>
        implements PiLevelFacetDescriptor<T, S>, PiLevelFacetType<T> {
    private final ResourceLocation id;
    private final Class<T> facetClass;
    private final Class<S> stateType;
    private final String storageId;

    protected PiGeneratedLevelFacetDescriptor(ResourceLocation id, Class<T> facetClass, Class<S> stateType) {
        this.id = Objects.requireNonNull(id, "id");
        this.facetClass = Objects.requireNonNull(facetClass, "facetClass");
        this.stateType = Objects.requireNonNull(stateType, "stateType");
        this.storageId = (id.getNamespace() + "__" + id.getPath()).replaceAll("[^a-z0-9._-]", "_");
    }

    @Override
    public final ResourceLocation id() {
        return id;
    }

    @Override
    public final Class<T> facetClass() {
        return facetClass;
    }

    @Override
    public final Class<S> stateType() {
        return stateType;
    }

    @Override
    public final String storageId() {
        return storageId;
    }

    @Override
    public final boolean isRegistered() {
        return PiActiveLevelFacetRegistry.isRegistered(facetClass);
    }

    @Override
    public final Optional<T> find(ServerLevel level) {
        return PiLevelFacetStorages.require().find(level, this);
    }

    @Override
    public final T get(ServerLevel level) {
        return PiLevelFacetStorages.require().resolve(level, this);
    }
}
