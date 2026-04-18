package org.pickaid.pibrary.api.entity;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;

/**
 * Global accessor for the installed {@link PiVehicleLifecycleService}.
 */
public final class PiVehicleLifecycleServices {
    public static final PibraryServiceKey<PiVehicleLifecycleService> KEY =
            new PibraryServiceKey<>(Pibrary.id("vehicle_lifecycle_service"), PiVehicleLifecycleService.class);

    private PiVehicleLifecycleServices() {
    }

    /**
     * Installs the global vehicle lifecycle service.
     *
     * @param service service instance or {@code null} to remove
     */
    public static void install(PiVehicleLifecycleService service) {
        PibraryServices.install(KEY, service);
    }

    /**
     * Finds the global vehicle lifecycle service.
     *
     * @return installed service, if present
     */
    public static Optional<PiVehicleLifecycleService> find() {
        return PibraryServices.findService(KEY);
    }

    /**
     * Requires the global vehicle lifecycle service.
     *
     * @return installed service
     */
    public static PiVehicleLifecycleService require() {
        return PibraryServices.requireService(KEY);
    }

    /**
     * Registers a handler on the global vehicle lifecycle service.
     *
     * @param entityType entity type to match
     * @param handler lifecycle handler
     * @param <E> supported entity type
     */
    public static <E extends Entity> void register(Class<E> entityType, PiVehicleLifecycleHandler<? super E> handler) {
        require().register(Objects.requireNonNull(entityType, "entityType"), Objects.requireNonNull(handler, "handler"));
    }
}
