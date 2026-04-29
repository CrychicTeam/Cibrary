package org.pickaid.pibrary.dev.example;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.pickaid.pibrary.api.projectile.PiProjectileImpactContext;
import org.pickaid.pibrary.api.projectile.PiProjectileImpactServices;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycleContext;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycleServices;

/**
 * Sample bootstrap showing how projectile lifecycle and impact hooks can both
 * feed a living facet.
 */
public final class CounterProjectileHooks {
    private CounterProjectileHooks() {
    }

    /**
     * Registers sample hooks for {@link AbstractArrow}.
     */
    public static void bootstrap() {
        PiProjectileLifecycleServices.register(AbstractArrow.class, new org.pickaid.pibrary.api.projectile.PiProjectileLifecycleHandler<>() {
            @Override
            public void onJoinLevel(AbstractArrow projectile, PiProjectileLifecycleContext context) {
                onArrowJoinLevel(projectile, context);
            }
        });
        PiProjectileImpactServices.register(AbstractArrow.class, new org.pickaid.pibrary.api.projectile.PiProjectileImpactHandler<>() {
            @Override
            public void onImpact(AbstractArrow projectile, PiProjectileImpactContext context) {
                onArrowImpact(projectile, context);
            }
        });
    }

    private static void onArrowJoinLevel(AbstractArrow arrow, PiProjectileLifecycleContext context) {
        if (context.loadedFromDisk() || arrow.level().isClientSide()) {
            return;
        }
        if (arrow.getOwner() instanceof Player player) {
            CounterPlayerFacet.get(player).increment();
        }
    }

    private static void onArrowImpact(AbstractArrow arrow, PiProjectileImpactContext context) {
        if (arrow.level().isClientSide()) {
            return;
        }
        if (arrow.getOwner() instanceof Player player) {
            CounterPlayerFacet.get(player).gainEnergy(1);
        }
    }
}
