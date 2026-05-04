package org.pickaid.pibrary.runtime.sync;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.pickaid.pinet.api.channel.PiChannelId;
import org.pickaid.pinet.api.service.PiNetService;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelope;
import org.pickaid.pibrary.api.sync.PiSyncBridge;

/**
 * Default {@link PiSyncBridge} implementation backed by {@code PiNet}.
 */
public final class PiNetSyncBridge implements PiSyncBridge {
    private static final PiChannelId SYNC_CHANNEL =
            new PiChannelId(new ResourceLocation("pibrary", "sync"));

    private final PiNetService netService;

    /**
     * Creates a sync bridge using the provided PiNet service.
     *
     * @param netService PiNet service used for message delivery
     */
    public PiNetSyncBridge(PiNetService netService) {
        this.netService = netService;
    }

    @Override
    public void syncOwner(ServerPlayer player, PiSyncEnvelope envelope) {
        netService.sendToPlayer(player, SYNC_CHANNEL, envelope);
    }

    @Override
    public void syncPlayer(ServerPlayer player, PiSyncEnvelope envelope) {
        netService.sendToPlayer(player, SYNC_CHANNEL, envelope);
    }

    @Override
    public void syncTracking(Entity anchor, PiSyncEnvelope envelope) {
        netService.sendToTracking(anchor, SYNC_CHANNEL, envelope);
    }

    @Override
    public void syncGlobal(PiSyncEnvelope envelope) {
        netService.broadcast(SYNC_CHANNEL, envelope);
    }
}
