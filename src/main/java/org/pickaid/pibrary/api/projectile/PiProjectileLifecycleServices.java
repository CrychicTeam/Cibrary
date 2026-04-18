package org.pickaid.pibrary.api.projectile;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.projectile.Projectile;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;

/**
 * Global accessor for the installed {@link PiProjectileLifecycleService}.
 */
public final class PiProjectileLifecycleServices {
    public static final PibraryServiceKey<PiProjectileLifecycleService> KEY =
            new PibraryServiceKey<>(Pibrary.id("projectile_lifecycle_service"), PiProjectileLifecycleService.class);

    private PiProjectileLifecycleServices() {
    }

    /**
     * Installs the global projectile lifecycle service.
     *
     * @param service service instance or {@code null} to remove
     */
    public static void install(PiProjectileLifecycleService service) {
        PibraryServices.install(KEY, service);
    }

    /**
     * Finds the global projectile lifecycle service.
     *
     * @return installed service, if present
     */
    public static Optional<PiProjectileLifecycleService> find() {
        return PibraryServices.findService(KEY);
    }

    /**
     * Requires the global projectile lifecycle service.
     *
     * @return installed service
     */
    public static PiProjectileLifecycleService require() {
        return PibraryServices.requireService(KEY);
    }

    /**
     * Registers a handler on the global projectile lifecycle service.
     *
     * @param projectileType projectile type to match
     * @param handler lifecycle handler
     * @param <P> supported projectile type
     */
    public static <P extends Projectile> void register(Class<P> projectileType, PiProjectileLifecycleHandler<? super P> handler) {
        require().register(Objects.requireNonNull(projectileType, "projectileType"), Objects.requireNonNull(handler, "handler"));
    }
}
