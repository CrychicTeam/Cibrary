package org.pickaid.pibrary.runtime.facet;

import java.util.Optional;
import java.util.Objects;
import net.minecraft.server.level.ServerLevel;
import org.pickaid.pibrary.api.facet.PiLevelFacetContext;
import org.pickaid.pibrary.api.facet.PiLevelFacetDescriptor;
import org.pickaid.pibrary.api.facet.PiLevelFacetStorage;
import org.pickaid.pibrary.api.facet.PiStateLevelFacet;

/**
 * Default level-facet storage backend using vanilla SavedData.
 */
public final class PiSavedDataLevelFacetStorage implements PiLevelFacetStorage {
    @Override
    public <T extends PiStateLevelFacet<?>> Optional<T> find(ServerLevel level, PiLevelFacetDescriptor<T, ?> descriptor) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(descriptor, "descriptor");
        PiLevelFacetContext context = new PiLevelFacetContext(level, PiLevelFacetContexts.shared(level));
        PiLevelFacetSavedData<T> data = level.getDataStorage().get(
                tag -> new PiLevelFacetSavedData<>(descriptor, context, tag),
                descriptor.storageId()
        );
        return Optional.ofNullable(data).map(PiLevelFacetSavedData::facet);
    }

    @Override
    public <T extends PiStateLevelFacet<?>> T resolve(ServerLevel level, PiLevelFacetDescriptor<T, ?> descriptor) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(descriptor, "descriptor");
        PiLevelFacetContext context = new PiLevelFacetContext(level, PiLevelFacetContexts.shared(level));
        PiLevelFacetSavedData<T> data = level.getDataStorage().computeIfAbsent(
                tag -> new PiLevelFacetSavedData<>(descriptor, context, tag),
                () -> new PiLevelFacetSavedData<>(descriptor, context),
                descriptor.storageId()
        );
        return data.facet();
    }
}
