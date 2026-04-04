package org.pickaid.pibrary.runtime.service;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

public final class PiLivingServiceInstanceProvider<T extends PiStateLivingEntityService<S>, S>
        implements ICapabilitySerializable<CompoundTag> {
    private final PiGeneratedLivingServiceDescriptor<T, S> descriptor;
    private final T service;
    private final LazyOptional<T> instance;

    public PiLivingServiceInstanceProvider(@Nullable LivingEntity living, PiGeneratedLivingServiceDescriptor<T, S> descriptor) {
        this.descriptor = descriptor;
        this.service = descriptor.create(descriptor.context(living));
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
