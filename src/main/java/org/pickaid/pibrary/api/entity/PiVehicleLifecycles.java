package org.pickaid.pibrary.api.entity;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryScopeKey;
import org.pickaid.pibrary.api.core.PibraryScopes;

/**
 * Global vehicle lifecycle registry.
 *
 * <p>This is the preferred public entry point for mount and dismount hooks.</p>
 */
public final class PiVehicleLifecycles {
    public static final PibraryScopeKey<PiVehicleLifecycleRegistry> KEY =
            new PibraryScopeKey<>(Pibrary.id("vehicle_lifecycle"), PiVehicleLifecycleRegistry.class);

    private PiVehicleLifecycles() {
    }

    public static void install(PiVehicleLifecycleRegistry lifecycle) {
        PibraryScopes.install(KEY, lifecycle);
    }

    public static Optional<PiVehicleLifecycleRegistry> find() {
        return PibraryScopes.findGlobal(KEY);
    }

    public static PiVehicleLifecycleRegistry require() {
        return PibraryScopes.requireGlobal(KEY);
    }

    public static <E extends Entity> void register(Class<E> entityType, PiVehicleLifecycleHandler<? super E> handler) {
        require().register(Objects.requireNonNull(entityType, "entityType"), Objects.requireNonNull(handler, "handler"));
    }
}
