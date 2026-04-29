package org.pickaid.pibrary.api.facet;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;

/**
 * Runtime backend that resolves typed level-scoped facets for one server level.
 */
public interface PiLevelFacetStorage {
    /**
     * Finds an existing facet instance without forcing creation.
     *
     * @param level authoritative server level
     * @param descriptor typed level-facet descriptor
     * @param <T> level facet type
     * @return resolved level facet, if already present
     */
    default <T extends PiStateLevelFacet<?>> Optional<T> find(ServerLevel level, PiLevelFacetDescriptor<T, ?> descriptor) {
        return Optional.of(resolve(level, descriptor));
    }

    /**
     * Resolves or creates the facet instance stored for the given level and descriptor.
     *
     * @param level authoritative server level
     * @param descriptor typed level-facet descriptor
     * @param <T> level facet type
     * @return resolved level facet
     */
    <T extends PiStateLevelFacet<?>> T resolve(ServerLevel level, PiLevelFacetDescriptor<T, ?> descriptor);
}
