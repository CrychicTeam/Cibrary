package org.pickaid.pibrary.runtime.facet;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.facet.PiChunkFacetContainer;
import org.pickaid.pibrary.api.facet.PiChunkFacetContext;
import org.pickaid.pibrary.api.facet.PiChunkFacetDescriptor;
import org.pickaid.pibrary.api.facet.PiChunkFacetType;
import org.pickaid.pibrary.api.facet.PiStateChunkFacet;

public abstract class PiGeneratedChunkFacetDescriptor<T extends PiStateChunkFacet<S>, S>
        implements PiChunkFacetDescriptor<T, S>, PiChunkFacetType<T> {
    private final ResourceLocation id;
    private final Class<T> facetClass;
    private final Class<S> stateType;

    protected PiGeneratedChunkFacetDescriptor(ResourceLocation id, Class<T> facetClass, Class<S> stateType) {
        this.id = Objects.requireNonNull(id, "id");
        this.facetClass = Objects.requireNonNull(facetClass, "facetClass");
        this.stateType = Objects.requireNonNull(stateType, "stateType");
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
    public abstract Capability<T> capability();

    @Override
    public final boolean isRegistered() {
        return PiActiveChunkFacetRegistry.isRegistered(facetClass);
    }

    public ICapabilitySerializable<CompoundTag> createProvider(@Nullable LevelChunk chunk) {
        return createProvider(chunk, new PiAttachedChunkFacetContainer(chunk));
    }

    public ICapabilitySerializable<CompoundTag> createProvider(
            @Nullable LevelChunk chunk,
            PiChunkFacetContainer container
    ) {
        return new PiChunkFacetInstanceProvider<>(chunk, this, container);
    }

    @Override
    public Optional<T> find(LevelChunk chunk) {
        return chunk.getCapability(capability()).resolve();
    }

    public T require(LevelChunk chunk) {
        return get(chunk);
    }

    public void registerCapability(RegisterCapabilitiesEvent event) {
        event.register(facetClass);
    }

    public final PiChunkFacetContext context(@Nullable LevelChunk chunk) {
        return context(chunk, new PiAttachedChunkFacetContainer(chunk));
    }

    public final PiChunkFacetContext context(@Nullable LevelChunk chunk, PiChunkFacetContainer container) {
        return new PiChunkFacetContext(chunk, container);
    }
}
