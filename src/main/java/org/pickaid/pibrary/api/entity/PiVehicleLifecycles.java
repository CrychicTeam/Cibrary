package org.pickaid.pibrary.api.entity;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;

/**
 * Global vehicle lifecycle registry.
 *
 * <p>This is the preferred public entry point for mount and dismount hooks.</p>
 */
public final class PiVehicleLifecycles {
    public static final PibraryServiceKey<PiVehicleLifecycleRegistry> KEY =
            new PibraryServiceKey<>(Pibrary.id("vehicle_lifecycle"), PiVehicleLifecycleRegistry.class);

    private PiVehicleLifecycles() {
    }

    public static void install(PiVehicleLifecycleRegistry lifecycle) {
        PibraryServices.install(KEY, lifecycle);
    }

    public static Optional<PiVehicleLifecycleRegistry> find() {
        return PibraryServices.findService(KEY);
    }

    public static PiVehicleLifecycleRegistry require() {
        return PibraryServices.requireService(KEY);
    }

    public static <E extends Entity> void register(Class<E> entityType, PiVehicleLifecycleHandler<? super E> handler) {
        require().register(Objects.requireNonNull(entityType, "entityType"), Objects.requireNonNull(handler, "handler"));
    }
}
