package org.pickaid.pibrary.runtime.chunk;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import org.pickaid.pibrary.api.service.PiChunkServiceContext;
import org.pickaid.pibrary.api.service.PiChunkServiceDescriptor;
import org.pickaid.pibrary.api.service.PiChunkServiceType;
import org.pickaid.pibrary.api.service.PiStateChunkService;

public abstract class PiGeneratedChunkServiceDescriptor<T extends PiStateChunkService<S>, S>
        implements PiChunkServiceDescriptor<T, S>, PiChunkServiceType<T> {
    private final ResourceLocation id;
    private final Class<T> serviceType;
    private final Class<S> stateType;
    private final String storageId;

    protected PiGeneratedChunkServiceDescriptor(ResourceLocation id, Class<T> serviceType, Class<S> stateType) {
        this.id = Objects.requireNonNull(id, "id");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
        this.stateType = Objects.requireNonNull(stateType, "stateType");
        this.storageId = (id.getNamespace() + "__" + id.getPath()).replaceAll("[^a-z0-9._-]", "_");
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

    @Override
    public final String storageId() {
        return storageId;
    }

    public abstract Capability<T> capability();

    @Override
    public final boolean isRegistered() {
        return PiActiveChunkServiceRegistry.isRegistered(serviceType);
    }

    public ICapabilitySerializable<CompoundTag> createProvider(LevelChunk chunk) {
        return new PiChunkServiceInstanceProvider<>(chunk, this);
    }

    @Override
    public Optional<T> find(LevelChunk chunk) {
        return Objects.requireNonNull(chunk, "chunk").getCapability(capability()).resolve();
    }

    public void registerCapability(RegisterCapabilitiesEvent event) {
        event.register(serviceType);
    }

    public final PiChunkServiceContext context(LevelChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        if (!(chunk.getLevel() instanceof ServerLevel level)) {
            throw new IllegalStateException("Pi chunk service " + serviceType.getName() + " requires a server level");
        }
        return new PiChunkServiceContext(chunk, level, PiChunkServiceContexts.shared(chunk));
    }
}
