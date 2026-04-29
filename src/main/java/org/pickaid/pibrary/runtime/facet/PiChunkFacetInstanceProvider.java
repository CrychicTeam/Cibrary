package org.pickaid.pibrary.runtime.facet;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.facet.PiChunkFacetContainer;
import org.pickaid.pibrary.api.facet.PiStateChunkFacet;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

public final class PiChunkFacetInstanceProvider<T extends PiStateChunkFacet<S>, S>
        implements ICapabilitySerializable<CompoundTag> {
    private final PiGeneratedChunkFacetDescriptor<T, S> descriptor;
    private final T facet;
    private final LazyOptional<T> instance;

    public PiChunkFacetInstanceProvider(
            @Nullable LevelChunk chunk,
            PiGeneratedChunkFacetDescriptor<T, S> descriptor,
            PiChunkFacetContainer container
    ) {
        this.descriptor = descriptor;
        this.facet = descriptor.create(descriptor.context(chunk, container));
        if (container instanceof PiAttachedChunkFacetContainer attachedContainer) {
            attachedContainer.attach(descriptor.facetClass(), facet);
        }
        this.instance = LazyOptional.of(() -> facet);
    }

    @Override
    public @NotNull <U> LazyOptional<U> getCapability(@NotNull Capability<U> capability, @Nullable Direction side) {
        return descriptor.capability().orEmpty(capability, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return facet.savePersistentData();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        facet.loadPersistentData(nbt, PiDecodeContext.strict());
    }

    public void invalidate() {
        instance.invalidate();
    }

    public T facet() {
        return facet;
    }
}
