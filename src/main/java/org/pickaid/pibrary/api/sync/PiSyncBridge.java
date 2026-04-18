package org.pickaid.pibrary.api.sync;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelope;

/**
 * Bridge from Pibrary sync planning to an actual networking runtime.
 */
public interface PiSyncBridge {
    /**
     * Sends a sync envelope to the owning player of a service host.
     *
     * @param player target player
     * @param envelope sync payload
     */
    void syncOwner(ServerPlayer player, PiSyncEnvelope envelope);

    /**
     * Sends a sync envelope to a specific player.
     *
     * @param player target player
     * @param envelope sync payload
     */
    void syncPlayer(ServerPlayer player, PiSyncEnvelope envelope);

    /**
     * Sends a sync envelope to players tracking the given anchor entity.
     *
     * @param anchor tracked entity
     * @param envelope sync payload
     */
    void syncTracking(Entity anchor, PiSyncEnvelope envelope);

    /**
     * Broadcasts a sync envelope globally.
     *
     * @param envelope sync payload
     */
    void syncGlobal(PiSyncEnvelope envelope);
}
