package org.pickaid.pibrary.api.targeting;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;

/**
 * Global accessor for the installed {@link PiTargetingService}.
 */
public final class PiTargetingServices {
    public static final PibraryServiceKey<PiTargetingService> KEY =
            new PibraryServiceKey<>(Pibrary.id("targeting_service"), PiTargetingService.class);

    private PiTargetingServices() {
    }

    /**
     * Installs the global targeting service.
     *
     * @param service service instance or {@code null} to remove
     */
    public static void install(PiTargetingService service) {
        PibraryServices.install(KEY, service);
    }

    /**
     * Finds the global targeting service.
     *
     * @return installed service, if present
     */
    public static Optional<PiTargetingService> find() {
        return PibraryServices.findService(KEY);
    }

    /**
     * Requires the global targeting service.
     *
     * @return installed service
     */
    public static PiTargetingService require() {
        return PibraryServices.requireService(KEY);
    }

    /**
     * Resolves a target query using the installed service.
     *
     * @param caster casting entity
     * @param query immutable target query
     * @return immutable ordered list of matching entities
     */
    public static List<Entity> resolve(Entity caster, PiTargetQuery query) {
        return require().resolve(Objects.requireNonNull(caster, "caster"), Objects.requireNonNull(query, "query"));
    }
}
