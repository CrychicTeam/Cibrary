package org.pickaid.pibrary.runtime.projectile;

import java.util.Objects;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.projectile.PiProjectileImpactContext;
import org.pickaid.pibrary.api.projectile.PiProjectileImpactResult;
import org.pickaid.pibrary.api.projectile.PiProjectileImpactServices;

/**
 * Forge event bridge for projectile impact callbacks.
 */
@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PiProjectileImpactEvents {
    private PiProjectileImpactEvents() {
    }

    /**
     * Bridges {@link ProjectileImpactEvent} into the installed projectile impact service.
     *
     * @param event Forge impact event
     */
    @SubscribeEvent
    @SuppressWarnings("removal")
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        PiProjectileImpactServices.find().ifPresent(service -> {
            PiProjectileImpactContext context = new PiProjectileImpactContext(event.getRayTraceResult());
            service.onImpact(event.getProjectile(), context);
            if (context.isCanceled()) {
                // Cancel is still the Forge 1.20.1 way to let the projectile continue flying.
                event.setCanceled(true);
                return;
            }
            event.setImpactResult(toForgeResult(context.result()));
        });
    }

    /**
     * Maps abstract Pibrary impact decisions onto Forge's concrete impact result enum.
     *
     * @param result abstract Pibrary impact result
     * @return Forge impact result
     */
    static ProjectileImpactEvent.ImpactResult toForgeResult(PiProjectileImpactResult result) {
        return switch (Objects.requireNonNull(result, "result")) {
            case DEFAULT -> ProjectileImpactEvent.ImpactResult.DEFAULT;
            case SKIP_ENTITY -> ProjectileImpactEvent.ImpactResult.SKIP_ENTITY;
            case STOP_AT_CURRENT -> ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT;
            case STOP_AT_CURRENT_NO_DAMAGE -> ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE;
        };
    }
}
