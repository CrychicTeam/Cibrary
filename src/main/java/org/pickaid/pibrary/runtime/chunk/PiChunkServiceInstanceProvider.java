package org.pickaid.pibrary.runtime.chunk;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.service.PiStateChunkService;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

public final class PiChunkServiceInstanceProvider<T extends PiStateChunkService<S>, S>
        implements ICapabilitySerializable<CompoundTag> {
    private final PiGeneratedChunkServiceDescriptor<T, S> descriptor;
    private final T service;
    private final LazyOptional<T> instance;

    public PiChunkServiceInstanceProvider(LevelChunk chunk, PiGeneratedChunkServiceDescriptor<T, S> descriptor) {
        this.descriptor = descriptor;
        this.service = descriptor.create(descriptor.context(chunk));
        this.instance = LazyOptional.of(() -> service);
    }

    @Override
    public @NotNull <U> LazyOptional<U> getCapability(@NotNull Capability<U> capability, @Nullable Direction side) {
        return descriptor.capability().orEmpty(capability, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return service.savePersistentData();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        service.loadPersistentData(nbt, PiDecodeContext.strict());
    }

    public void invalidate() {
        instance.invalidate();
    }

    public T service() {
        return service;
    }
}
