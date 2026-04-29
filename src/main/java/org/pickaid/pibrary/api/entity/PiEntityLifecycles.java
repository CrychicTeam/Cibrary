package org.pickaid.pibrary.api.entity;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;

/**
 * Global entity lifecycle registry.
 *
 * <p>This is the preferred public entry point for registering entity lifecycle
 * hooks. It hides the low-level registry plumbing behind a game-facing name.</p>
 */
public final class PiEntityLifecycles {
    public static final PibraryServiceKey<PiEntityLifecycleRegistry> KEY =
            new PibraryServiceKey<>(Pibrary.id("entity_lifecycle"), PiEntityLifecycleRegistry.class);

    private PiEntityLifecycles() {
    }

    public static void install(PiEntityLifecycleRegistry lifecycle) {
        PibraryServices.install(KEY, lifecycle);
    }

    public static Optional<PiEntityLifecycleRegistry> find() {
        return PibraryServices.findService(KEY);
    }

    public static PiEntityLifecycleRegistry require() {
        return PibraryServices.requireService(KEY);
    }

    public static <E extends Entity> void register(Class<E> entityType, PiEntityLifecycleHandler<? super E> handler) {
        require().register(Objects.requireNonNull(entityType, "entityType"), Objects.requireNonNull(handler, "handler"));
    }
}
