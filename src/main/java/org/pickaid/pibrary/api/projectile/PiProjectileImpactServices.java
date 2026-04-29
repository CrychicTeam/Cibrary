package org.pickaid.pibrary.api.projectile;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.projectile.Projectile;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;

/**
 * Global accessor for the installed {@link PiProjectileImpactService}.
 */
public final class PiProjectileImpactServices {
    public static final PibraryServiceKey<PiProjectileImpactService> KEY =
            new PibraryServiceKey<>(Pibrary.id("projectile_impact_service"), PiProjectileImpactService.class);

    private PiProjectileImpactServices() {
    }

    /**
     * Installs the global projectile impact service.
     *
     * @param service service instance or {@code null} to remove
     */
    public static void install(PiProjectileImpactService service) {
        PibraryServices.install(KEY, service);
    }

    /**
     * Finds the global projectile impact service.
     *
     * @return installed service, if present
     */
    public static Optional<PiProjectileImpactService> find() {
        return PibraryServices.findService(KEY);
    }

    /**
     * Requires the global projectile impact service.
     *
     * @return installed service
     */
    public static PiProjectileImpactService require() {
        return PibraryServices.requireService(KEY);
    }

    /**
     * Registers a handler on the global projectile impact service.
     *
     * @param projectileType projectile type to match
     * @param handler impact handler
     * @param <P> supported projectile type
     */
    public static <P extends Projectile> void register(Class<P> projectileType, PiProjectileImpactHandler<? super P> handler) {
        require().register(Objects.requireNonNull(projectileType, "projectileType"), Objects.requireNonNull(handler, "handler"));
    }
}
