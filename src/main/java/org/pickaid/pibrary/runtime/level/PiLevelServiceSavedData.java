package org.pickaid.pibrary.runtime.level;

import java.util.HashSet;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.pickaid.pibrary.api.service.PiLevelServiceContext;
import org.pickaid.pibrary.api.service.PiLevelServiceDescriptor;
import org.pickaid.pibrary.api.service.PiStateLevelService;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

/**
 * Generic SavedData wrapper that persists one typed Pi level service.
 *
 * @param <T> level service type
 */
final class PiLevelServiceSavedData<T extends PiStateLevelService<?>> extends SavedData {
    private final T service;
    private CompoundTag persistedSnapshot;

    PiLevelServiceSavedData(PiLevelServiceDescriptor<T, ?> descriptor, PiLevelServiceContext context) {
        Objects.requireNonNull(descriptor, "descriptor");
        this.service = descriptor.create(Objects.requireNonNull(context, "context"));
        this.persistedSnapshot = service.savePersistentData().copy();
    }

    PiLevelServiceSavedData(PiLevelServiceDescriptor<T, ?> descriptor, PiLevelServiceContext context, CompoundTag tag) {
        this(descriptor, context);
        service.loadPersistentData(Objects.requireNonNull(tag, "tag"), PiDecodeContext.strict());
        this.persistedSnapshot = service.savePersistentData().copy();
    }

    T service() {
        return service;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag persisted = service.savePersistentData();
        for (String key : new HashSet<>(tag.getAllKeys())) {
            tag.remove(key);
        }
        tag.merge(persisted);
        persistedSnapshot = persisted.copy();
        return tag;
    }

    @Override
    public boolean isDirty() {
        return !service.savePersistentData().equals(persistedSnapshot);
    }
}
