package org.pickaid.pibrary.runtime.facet;

import java.util.HashSet;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.pickaid.pibrary.api.facet.PiLevelFacetContext;
import org.pickaid.pibrary.api.facet.PiLevelFacetDescriptor;
import org.pickaid.pibrary.api.facet.PiStateLevelFacet;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

/**
 * Generic SavedData wrapper that persists one typed Pi level facet.
 *
 * @param <T> level facet type
 */
final class PiLevelFacetSavedData<T extends PiStateLevelFacet<?>> extends SavedData {
    private final T facet;
    private CompoundTag persistedSnapshot;

    PiLevelFacetSavedData(PiLevelFacetDescriptor<T, ?> descriptor, PiLevelFacetContext context) {
        Objects.requireNonNull(descriptor, "descriptor");
        this.facet = descriptor.create(Objects.requireNonNull(context, "context"));
        this.persistedSnapshot = facet.savePersistentData().copy();
    }

    PiLevelFacetSavedData(PiLevelFacetDescriptor<T, ?> descriptor, PiLevelFacetContext context, CompoundTag tag) {
        this(descriptor, context);
        facet.loadPersistentData(Objects.requireNonNull(tag, "tag"), PiDecodeContext.strict());
        this.persistedSnapshot = facet.savePersistentData().copy();
    }

    T facet() {
        return facet;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag persisted = facet.savePersistentData();
        for (String key : new HashSet<>(tag.getAllKeys())) {
            tag.remove(key);
        }
        tag.merge(persisted);
        persistedSnapshot = persisted.copy();
        return tag;
    }

    @Override
    public boolean isDirty() {
        return !facet.savePersistentData().equals(persistedSnapshot);
    }
}
