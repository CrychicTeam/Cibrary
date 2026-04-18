package org.pickaid.pibrary.runtime.service;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.service.PiLivingServiceHost;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

/**
 * Capability provider wrapping one generated living service instance.
 *
 * @param <T> service type
 * @param <S> backing state type
 */
public final class PiLivingServiceInstanceProvider<T extends PiStateLivingEntityService<S>, S>
        implements ICapabilitySerializable<CompoundTag> {
    private final PiGeneratedLivingServiceDescriptor<T, S> descriptor;
    private final T service;
    private final LazyOptional<T> instance;

    /**
     * Creates the provider, instantiates the service, attaches it to the host when
     * possible, and runs attach lifecycle hooks.
     *
     * @param living owning entity, when available
     * @param descriptor generated service descriptor
     * @param host living service host
     */
    public PiLivingServiceInstanceProvider(
            @Nullable LivingEntity living,
            PiGeneratedLivingServiceDescriptor<T, S> descriptor,
            PiLivingServiceHost host
    ) {
        this.descriptor = descriptor;
        this.service = descriptor.create(descriptor.context(living, host));
        if (host instanceof PiAttachedLivingHost attachedHost) {
            attachedHost.attach(descriptor.serviceType(), service);
        }
        PiLivingServiceLifecycles.onAttached(service);
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

    /**
     * Invalidates the exposed lazy capability wrapper.
     */
    public void invalidate() {
        instance.invalidate();
    }

    /**
     * Returns the created service instance.
     *
     * @return service instance
     */
    public T service() {
        return service;
    }
}
