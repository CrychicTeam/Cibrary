package org.pickaid.pibrary.api.projectile;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.projectile.Projectile;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;

/**
 * Global accessor for the installed {@link PiProjectileImpactRegistry}.
 */
public final class PiProjectileImpacts {
    public static final PibraryServiceKey<PiProjectileImpactRegistry> KEY =
            new PibraryServiceKey<>(Pibrary.id("projectile_impact"), PiProjectileImpactRegistry.class);

    private PiProjectileImpacts() {
    }

    /**
     * Installs the global projectile impact registry.
     *
     * @param registry registry instance or {@code null} to remove
     */
    public static void install(PiProjectileImpactRegistry registry) {
        PibraryServices.install(KEY, registry);
    }

    /**
     * Finds the global projectile impact registry.
     *
     * @return installed registry, if present
     */
    public static Optional<PiProjectileImpactRegistry> find() {
        return PibraryServices.findService(KEY);
    }

    /**
     * Requires the global projectile impact registry.
     *
     * @return installed registry
     */
    public static PiProjectileImpactRegistry require() {
        return PibraryServices.requireService(KEY);
    }

    /**
     * Registers a handler on the global projectile impact registry.
     *
     * @param projectileType projectile type to match
     * @param handler impact handler
     * @param <P> supported projectile type
     */
    public static <P extends Projectile> void register(Class<P> projectileType, PiProjectileImpactHandler<? super P> handler) {
        require().register(Objects.requireNonNull(projectileType, "projectileType"), Objects.requireNonNull(handler, "handler"));
    }
}
