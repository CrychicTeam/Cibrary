package org.pickaid.pibrary.runtime.level;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.pickaid.pibrary.api.service.PiLevelServiceDescriptor;
import org.pickaid.pibrary.api.service.PiLevelServiceType;
import org.pickaid.pibrary.api.service.PiLevelServiceStorages;
import org.pickaid.pibrary.api.service.PiStateLevelService;

public abstract class PiGeneratedLevelServiceDescriptor<T extends PiStateLevelService<S>, S>
        implements PiLevelServiceDescriptor<T, S>, PiLevelServiceType<T> {
    private final ResourceLocation id;
    private final Class<T> serviceType;
    private final Class<S> stateType;
    private final String storageId;

    protected PiGeneratedLevelServiceDescriptor(ResourceLocation id, Class<T> serviceType, Class<S> stateType) {
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

    @Override
    public final boolean isRegistered() {
        return PiActiveLevelServiceRegistry.isRegistered(serviceType);
    }

    @Override
    public final Optional<T> find(ServerLevel level) {
        return PiLevelServiceStorages.require().find(level, this);
    }

    @Override
    public final T get(ServerLevel level) {
        return PiLevelServiceStorages.require().resolve(level, this);
    }
}
