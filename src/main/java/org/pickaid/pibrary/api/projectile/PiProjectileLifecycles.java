package org.pickaid.pibrary.api.projectile;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.projectile.Projectile;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryScopeKey;
import org.pickaid.pibrary.api.core.PibraryScopes;

/**
 * Global accessor for the installed {@link PiProjectileLifecycleRegistry}.
 */
public final class PiProjectileLifecycles {
    public static final PibraryScopeKey<PiProjectileLifecycleRegistry> KEY =
            new PibraryScopeKey<>(Pibrary.id("projectile_lifecycle"), PiProjectileLifecycleRegistry.class);

    private PiProjectileLifecycles() {
    }

    /**
     * Installs the global projectile lifecycle registry.
     *
     * @param registry registry instance or {@code null} to remove
     */
    public static void install(PiProjectileLifecycleRegistry registry) {
        PibraryScopes.install(KEY, registry);
    }

    /**
     * Finds the global projectile lifecycle registry.
     *
     * @return installed registry, if present
     */
    public static Optional<PiProjectileLifecycleRegistry> find() {
        return PibraryScopes.findGlobal(KEY);
    }

    /**
     * Requires the global projectile lifecycle registry.
     *
     * @return installed registry
     */
    public static PiProjectileLifecycleRegistry require() {
        return PibraryScopes.requireGlobal(KEY);
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
