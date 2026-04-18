package org.pickaid.pibrary.runtime.level;

import java.util.Optional;
import java.util.Objects;
import net.minecraft.server.level.ServerLevel;
import org.pickaid.pibrary.api.service.PiLevelServiceContext;
import org.pickaid.pibrary.api.service.PiLevelServiceDescriptor;
import org.pickaid.pibrary.api.service.PiLevelServiceStorage;
import org.pickaid.pibrary.api.service.PiStateLevelService;

/**
 * Default level-service storage backend using vanilla SavedData.
 */
public final class PiSavedDataLevelServiceStorage implements PiLevelServiceStorage {
    @Override
    public <T extends PiStateLevelService<?>> Optional<T> find(ServerLevel level, PiLevelServiceDescriptor<T, ?> descriptor) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(descriptor, "descriptor");
        PiLevelServiceContext context = new PiLevelServiceContext(level, PiLevelServiceContexts.shared(level));
        PiLevelServiceSavedData<T> data = level.getDataStorage().get(
                tag -> new PiLevelServiceSavedData<>(descriptor, context, tag),
                descriptor.storageId()
        );
        return Optional.ofNullable(data).map(PiLevelServiceSavedData::service);
    }

    @Override
    public <T extends PiStateLevelService<?>> T resolve(ServerLevel level, PiLevelServiceDescriptor<T, ?> descriptor) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(descriptor, "descriptor");
        PiLevelServiceContext context = new PiLevelServiceContext(level, PiLevelServiceContexts.shared(level));
        PiLevelServiceSavedData<T> data = level.getDataStorage().computeIfAbsent(
                tag -> new PiLevelServiceSavedData<>(descriptor, context, tag),
                () -> new PiLevelServiceSavedData<>(descriptor, context),
                descriptor.storageId()
        );
        return data.service();
    }
}
