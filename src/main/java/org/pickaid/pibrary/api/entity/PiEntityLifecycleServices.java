package org.pickaid.pibrary.api.entity;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;

/**
 * Global accessor for the installed {@link PiEntityLifecycleService}.
 */
public final class PiEntityLifecycleServices {
    public static final PibraryServiceKey<PiEntityLifecycleService> KEY =
            new PibraryServiceKey<>(Pibrary.id("entity_lifecycle_service"), PiEntityLifecycleService.class);

    private PiEntityLifecycleServices() {
    }

    /**
     * Installs the global entity lifecycle service.
     *
     * @param service service instance or {@code null} to remove
     */
    public static void install(PiEntityLifecycleService service) {
        PibraryServices.install(KEY, service);
    }

    /**
     * Finds the global entity lifecycle service.
     *
     * @return installed service, if present
     */
    public static Optional<PiEntityLifecycleService> find() {
        return PibraryServices.findService(KEY);
    }

    /**
     * Requires the global entity lifecycle service.
     *
     * @return installed service
     */
    public static PiEntityLifecycleService require() {
        return PibraryServices.requireService(KEY);
    }

    /**
     * Registers a handler on the global lifecycle service.
     *
     * @param entityType entity type to match
     * @param handler lifecycle handler
     * @param <E> supported entity type
     */
    public static <E extends Entity> void register(Class<E> entityType, PiEntityLifecycleHandler<? super E> handler) {
        require().register(Objects.requireNonNull(entityType, "entityType"), Objects.requireNonNull(handler, "handler"));
    }
}
