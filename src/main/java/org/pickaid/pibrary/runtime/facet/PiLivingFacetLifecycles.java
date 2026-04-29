package org.pickaid.pibrary.runtime.facet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.pibrary.api.presentation.PiPresentationSource;
import org.pickaid.pibrary.api.presentation.PiPresentations;
import org.pickaid.pibrary.api.facet.PiAttachedLivingFacet;
import org.pickaid.pibrary.api.facet.PiCloneAwareLivingFacet;
import org.pickaid.pibrary.api.facet.PiLivingEntityFacet;
import org.pickaid.pibrary.api.facet.PiSyncAwareLivingFacet;
import org.pickaid.pibrary.api.facet.PiTickingLivingFacet;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

/**
 * Dispatch helpers for optional living facet lifecycle extension interfaces.
 */
public final class PiLivingFacetLifecycles {
    private PiLivingFacetLifecycles() {
    }

    /**
     * Runs the attach hook when implemented by the facet.
     *
     * @param facet attached facet
     */
    public static void onAttached(PiLivingEntityFacet facet) {
        if (facet instanceof PiAttachedLivingFacet attached) {
            attached.onAttached();
        }
    }

    /**
     * Runs the appropriate client or server tick hook when implemented by the facet.
     *
     * @param facet ticking facet
     * @param clientSide whether the current side is the client
     */
    public static void tick(PiLivingEntityFacet facet, boolean clientSide) {
        if (facet instanceof PiTickingLivingFacet ticking) {
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
     * @param facet synced facet
     * @param kind full or delta sync
     * @param route route that delivered the payload
     */
    public static void onSyncApplied(PiLivingEntityFacet facet, PiSyncEnvelopeKind kind, PiSyncRoute route) {
        onSyncApplied(facet, kind, route, new CompoundTag(), PiDecodeContext.strict());
    }

    /**
     * Runs sync-applied hooks when implemented by the facet.
     *
     * @param facet synced facet
     * @param kind full or delta sync
     * @param route route that delivered the payload
     * @param payload raw payload that was applied
     * @param context decode context
     */
    public static void onSyncApplied(
            PiLivingEntityFacet facet,
            PiSyncEnvelopeKind kind,
            PiSyncRoute route,
            CompoundTag payload,
            PiDecodeContext context
    ) {
        if (facet instanceof PiSyncAwareLivingFacet aware) {
            aware.onSyncApplied(kind, route, payload, context);
        }
        if (facet instanceof PiPresentationSource presentationSource) {
            PiPresentations.invalidateClientApply(presentationSource);
        }
    }

    /**
     * Runs clone hooks when implemented by the facet.
     *
     * @param facet cloned facet
     * @param original original entity, when available
     * @param wasDeath whether the clone was created by death/respawn
     */
    public static void onCloned(PiLivingEntityFacet facet, @Nullable LivingEntity original, boolean wasDeath) {
        if (facet instanceof PiCloneAwareLivingFacet aware) {
            aware.onCloned(original, wasDeath);
        }
    }
}
