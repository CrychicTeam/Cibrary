package org.pickaid.pibrary.api.projectile;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.projectile.Projectile;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;

/**
 * Global accessor for the installed {@link PiProjectileLifecycleRegistry}.
 */
public final class PiProjectileLifecycles {
    public static final PibraryServiceKey<PiProjectileLifecycleRegistry> KEY =
            new PibraryServiceKey<>(Pibrary.id("projectile_lifecycle"), PiProjectileLifecycleRegistry.class);

    private PiProjectileLifecycles() {
    }

    /**
     * Installs the global projectile lifecycle registry.
     *
     * @param registry registry instance or {@code null} to remove
     */
    public static void install(PiProjectileLifecycleRegistry registry) {
        PibraryServices.install(KEY, registry);
    }

    /**
     * Finds the global projectile lifecycle registry.
     *
     * @return installed registry, if present
     */
    public static Optional<PiProjectileLifecycleRegistry> find() {
        return PibraryServices.findService(KEY);
    }

    /**
     * Requires the global projectile lifecycle registry.
     *
     * @return installed registry
     */
    public static PiProjectileLifecycleRegistry require() {
        return PibraryServices.requireService(KEY);
    }

    /**
     * Registers a handler on the global projectile lifecycle registry.
     *
     * @param projectileType projectile type to match
     * @param handler lifecycle handler
     * @param <P> supported projectile type
     */
    public static <P extends Projectile> void register(Class<P> projectileType, PiProjectileLifecycleHandler<? super P> handler) {
        require().register(Objects.requireNonNull(projectileType, "projectileType"), Objects.requireNonNull(handler, "handler"));
    }
}
