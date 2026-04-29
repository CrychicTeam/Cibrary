package org.pickaid.pibrary.runtime.sync;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;

/**
 * Clientbound sync packet carrying generated living-facet payloads.
 *
 * @param entityId target entity id
 * @param kind full or delta sync
 * @param route delivery route used for filtering
 * @param payload encoded generated-facet payloads
 */
public record PiLivingSyncPacket(
        int entityId,
        PiSyncEnvelopeKind kind,
        PiSyncRoute route,
        CompoundTag payload
) {
    public PiLivingSyncPacket {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(route, "route");
        Objects.requireNonNull(payload, "payload");
    }
}
