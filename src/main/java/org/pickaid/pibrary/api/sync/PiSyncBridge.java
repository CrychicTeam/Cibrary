package org.pickaid.pibrary.api.sync;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.pickaid.pinet.api.sync.PiSyncEnvelope;

public interface PiSyncBridge {
    void syncOwner(ServerPlayer player, PiSyncEnvelope envelope);

    void syncPlayer(ServerPlayer player, PiSyncEnvelope envelope);

    void syncTracking(Entity anchor, PiSyncEnvelope envelope);

    void syncGlobal(PiSyncEnvelope envelope);
}
