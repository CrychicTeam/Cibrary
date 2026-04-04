package org.pickaid.pibrary.runtime.service;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pinet.api.sync.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.PiSyncRoute;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import org.pickaid.pibrary.api.service.PiLivingServiceContext;
import org.pickaid.pibrary.api.service.PiLivingServiceDescriptor;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;

public abstract class PiGeneratedLivingServiceDescriptor<T extends PiStateLivingEntityService<S>, S>
        implements PiLivingServiceDescriptor<T, S> {
    private final ResourceLocation id;
    private final Class<T> serviceType;
    private final Class<S> stateType;

    protected PiGeneratedLivingServiceDescriptor(ResourceLocation id, Class<T> serviceType, Class<S> stateType) {
        this.id = Objects.requireNonNull(id, "id");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
        this.stateType = Objects.requireNonNull(stateType, "stateType");
    }

    @Override
    public final ResourceLocation id() {
        return id;
    }

    @Override
    public final Class<T> serviceType() {
        return serviceType;
    }

    @Override
    public final Class<S> stateType() {
        return stateType;
    }

    public abstract Capability<T> capability();

    public ICapabilitySerializable<CompoundTag> createProvider(@Nullable LivingEntity living) {
        return new PiLivingServiceInstanceProvider<>(living, this);
    }

    public Optional<T> find(LivingEntity living) {
        return living.getCapability(capability()).resolve();
    }

    public T require(LivingEntity living) {
        return find(living).orElseThrow(() ->
                new IllegalStateException("Missing Pi living service " + serviceType.getName() + " on " + living.getClass().getName()));
    }

    public void registerCapability(RegisterCapabilitiesEvent event) {
        event.register(serviceType);
    }

    public boolean hasDirty(LivingEntity living, PiSyncRoute route) {
        return find(living).map(service -> service.hasDirty(route)).orElse(false);
    }

    public CompoundTag buildSyncPayload(LivingEntity living, PiSyncEnvelopeKind kind, PiSyncRoute route) {
        return find(living)
                .map(service -> service.buildSyncPayload(kind, route))
                .orElseGet(CompoundTag::new);
    }

    public void applySyncPayload(LivingEntity living, PiSyncEnvelopeKind kind, PiSyncRoute route, CompoundTag payload) {
        find(living).ifPresent(service -> service.applySyncPayload(kind, payload, PiDecodeContext.strict()));
    }

    public void clearDirty(LivingEntity living, PiSyncRoute route) {
        find(living).ifPresent(service -> service.clearDirty(route));
    }

    public void copy(LivingEntity source, LivingEntity target) {
        Optional<T> sourceService = find(source);
        Optional<T> targetService = find(target);
        if (sourceService.isPresent() && targetService.isPresent()) {
            targetService.get().copyFrom(sourceService.get());
        }
    }

    public final PiLivingServiceContext context(@Nullable LivingEntity living) {
        return new PiLivingServiceContext(living, new PiLivingQueryHost(living));
    }
}
