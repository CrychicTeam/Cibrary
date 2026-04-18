package org.pickaid.pibrary.runtime.service;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.pibrary.api.presentation.PiPresentationHost;
import org.pickaid.pibrary.api.presentation.PiPresentations;
import org.pickaid.pibrary.api.service.PiAttachedLivingService;
import org.pickaid.pibrary.api.service.PiCloneAwareLivingService;
import org.pickaid.pibrary.api.service.PiLivingEntityService;
import org.pickaid.pibrary.api.service.PiSyncAwareLivingService;
import org.pickaid.pibrary.api.service.PiTickingLivingService;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

/**
 * Dispatch helpers for optional living service lifecycle extension interfaces.
 */
public final class PiLivingServiceLifecycles {
    private PiLivingServiceLifecycles() {
    }

    /**
     * Runs the attach hook when implemented by the service.
     *
     * @param service attached service
     */
    public static void onAttached(PiLivingEntityService service) {
        if (service instanceof PiAttachedLivingService attached) {
            attached.onAttached();
        }
    }

    /**
     * Runs the appropriate client or server tick hook when implemented by the service.
     *
     * @param service ticking service
     * @param clientSide whether the current side is the client
     */
    public static void tick(PiLivingEntityService service, boolean clientSide) {
        if (service instanceof PiTickingLivingService ticking) {
            if (clientSide) {
                ticking.clientTick();
            } else {
                ticking.serverTick();
            }
        }
    }

    /**
     * Runs sync-applied hooks with an empty payload.
     *
     * @param service synced service
     * @param kind full or delta sync
     * @param route route that delivered the payload
     */
    public static void onSyncApplied(PiLivingEntityService service, PiSyncEnvelopeKind kind, PiSyncRoute route) {
        onSyncApplied(service, kind, route, new CompoundTag(), PiDecodeContext.strict());
    }

    /**
     * Runs sync-applied hooks when implemented by the service.
     *
     * @param service synced service
     * @param kind full or delta sync
     * @param route route that delivered the payload
     * @param payload raw payload that was applied
     * @param context decode context
     */
    public static void onSyncApplied(
            PiLivingEntityService service,
            PiSyncEnvelopeKind kind,
            PiSyncRoute route,
            CompoundTag payload,
            PiDecodeContext context
    ) {
        if (service instanceof PiSyncAwareLivingService aware) {
            aware.onSyncApplied(kind, route, payload, context);
        }
        if (service instanceof PiPresentationHost presentationHost) {
            PiPresentations.invalidateClientApply(presentationHost);
        }
    }

    /**
     * Runs clone hooks when implemented by the service.
     *
     * @param service cloned service
     * @param original original entity, when available
     * @param wasDeath whether the clone was created by death/respawn
     */
    public static void onCloned(PiLivingEntityService service, @Nullable LivingEntity original, boolean wasDeath) {
        if (service instanceof PiCloneAwareLivingService aware) {
            aware.onCloned(original, wasDeath);
        }
    }
}
