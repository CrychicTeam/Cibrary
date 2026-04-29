package org.pickaid.pibrary.api.facet;

import net.minecraft.nbt.CompoundTag;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

/**
 * Optional lifecycle hook for facets that need to react after sync payloads are applied.
 */
public interface PiSyncAwareLivingFacet {
    /**
     * Called after a sync payload has been decoded into the facet state.
     *
     * @param kind full or delta sync
     * @param route route that delivered the payload
     * @param payload raw payload that was applied
     * @param context decode context used during application
     */
    void onSyncApplied(PiSyncEnvelopeKind kind, PiSyncRoute route, CompoundTag payload, PiDecodeContext context);
}
