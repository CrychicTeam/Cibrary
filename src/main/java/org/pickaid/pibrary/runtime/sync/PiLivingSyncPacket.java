package org.pickaid.pibrary.runtime.sync;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import org.pickaid.pinet.api.sync.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.PiSyncRoute;

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
