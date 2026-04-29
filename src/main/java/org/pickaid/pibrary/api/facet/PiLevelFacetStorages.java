package org.pickaid.pibrary.api.facet;

import java.util.Optional;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryScopeKey;
import org.pickaid.pibrary.api.core.PibraryScopes;

/**
 * Global accessor for the installed level-facet storage backend.
 */
public final class PiLevelFacetStorages {
    public static final PibraryScopeKey<PiLevelFacetStorage> KEY =
            new PibraryScopeKey<>(Pibrary.id("level_facet_storage"), PiLevelFacetStorage.class);

    private PiLevelFacetStorages() {
    }

    /**
     * Installs the global level-facet storage backend.
     *
     * @param storage storage backend or {@code null} to remove
     */
    public static void install(PiLevelFacetStorage storage) {
        PibraryScopes.install(KEY, storage);
    }

    /**
     * Finds the global level-facet storage backend.
     *
     * @return installed backend, if present
     */
    public static Optional<PiLevelFacetStorage> find() {
        return PibraryScopes.findGlobal(KEY);
    }

    /**
     * Requires the global level-facet storage backend.
     *
     * @return installed backend
     */
    public static PiLevelFacetStorage require() {
        return PibraryScopes.requireGlobal(KEY);
    }
}
