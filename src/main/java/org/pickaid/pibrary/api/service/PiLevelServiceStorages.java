package org.pickaid.pibrary.api.service;

import java.util.Optional;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;

/**
 * Global accessor for the installed level-service storage backend.
 */
public final class PiLevelServiceStorages {
    public static final PibraryServiceKey<PiLevelServiceStorage> KEY =
            new PibraryServiceKey<>(Pibrary.id("level_service_storage"), PiLevelServiceStorage.class);

    private PiLevelServiceStorages() {
    }

    /**
     * Installs the global level-service storage backend.
     *
     * @param storage storage backend or {@code null} to remove
     */
    public static void install(PiLevelServiceStorage storage) {
        PibraryServices.install(KEY, storage);
    }

    /**
     * Finds the global level-service storage backend.
     *
     * @return installed backend, if present
     */
    public static Optional<PiLevelServiceStorage> find() {
        return PibraryServices.findService(KEY);
    }

    /**
     * Requires the global level-service storage backend.
     *
     * @return installed backend
     */
    public static PiLevelServiceStorage require() {
        return PibraryServices.requireService(KEY);
    }
}
