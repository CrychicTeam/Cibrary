package org.pickaid.pibrary.runtime.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.pickaid.pinet.api.channel.PiChannelId;
import org.pickaid.pinet.api.message.PiMessageCodec;
import org.pickaid.pinet.api.message.PiMessageHandler;
import org.pickaid.pinet.api.message.buffer.PiMessageBuffer;
import org.pickaid.pinet.api.service.PiNetService;
import org.pickaid.pinet.api.service.PiNetServices;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.runtime.service.PiActiveLivingServiceRegistry;

/**
 * Networking bridge for generated living-service sync packets.
 */
public final class PiLivingSyncMessages {
    private static final PiChannelId CHANNEL = new PiChannelId(Pibrary.id("living_services"));
    private static final PiMessageCodec<PiLivingSyncPacket> CODEC = new PiMessageCodec<>() {
        @Override
        public void encode(PiMessageBuffer buffer, PiLivingSyncPacket value) {
            buffer.writeVarInt(value.entityId());
            buffer.writeEnum(value.kind());
            buffer.writeEnum(value.route());
            buffer.writeNbt(value.payload());
        }

        @Override
        public PiLivingSyncPacket decode(PiMessageBuffer buffer) {
            int entityId = buffer.readVarInt();
            PiSyncEnvelopeKind kind = buffer.readEnum(PiSyncEnvelopeKind.class);
            PiSyncRoute route = buffer.readEnum(PiSyncRoute.class);
            CompoundTag payload = buffer.readNbt();
            return new PiLivingSyncPacket(entityId, kind, route, payload == null ? new CompoundTag() : payload);
        }
    };
    private static final PiMessageHandler<PiLivingSyncPacket> HANDLER = (packet, context) -> {
        if (context.clientbound()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PiLivingSyncClient.handle(packet));
        }
    };

    private static volatile boolean registered;

    private PiLivingSyncMessages() {
    }

    /**
     * Ensures the play message channel is registered.
     */
    public static void bootstrap() {
        ensureRegistered();
    }

    /**
     * Sends a full sync payload for one living entity to a single player.
     *
     * @param living source living entity
     * @param player target player
     * @param route route to encode against
     */
    public static void sendFull(LivingEntity living, ServerPlayer player, PiSyncRoute route) {
        PiNetService netService = ensureRegistered();
        if (netService == null) {
            return;
        }
        CompoundTag payload = buildPayload(living, PiSyncEnvelopeKind.FULL, route, false);
        if (!hasServices(payload)) {
            return;
        }
        netService.sendToPlayer(player, CHANNEL, new PiLivingSyncPacket(living.getId(), PiSyncEnvelopeKind.FULL, route, payload));
    }

    /**
     * Sends a full tracking payload for one living entity to all tracking players.
     *
     * @param living source living entity
     */
    public static void sendTrackingFull(LivingEntity living) {
        PiNetService netService = ensureRegistered();
        if (netService == null) {
            return;
        }
        CompoundTag payload = buildPayload(living, PiSyncEnvelopeKind.FULL, PiSyncRoute.TRACKING, false);
        if (!hasServices(payload)) {
            return;
        }
        netService.sendToTracking(living, CHANNEL, new PiLivingSyncPacket(living.getId(), PiSyncEnvelopeKind.FULL, PiSyncRoute.TRACKING, payload));
    }

    /**
     * Sends the full owner and tracking payloads needed when a player logs in, respawns, or clones.
     *
     * @param player target player
     */
    public static void sendPlayerLifecycleFull(ServerPlayer player) {
        sendFull(player, player, PiSyncRoute.OWNER);
        sendTrackingFull(player);
    }

    /**
     * Flushes dirty owner and tracking deltas for one living entity.
     *
     * @param living source living entity
     */
    public static void flush(LivingEntity living) {
        if (living.level().isClientSide()) {
            return;
        }
        PiNetService netService = ensureRegistered();
        if (netService == null) {
            return;
        }
        boolean ownerSent = false;
        boolean trackingSent = false;
        if (living instanceof ServerPlayer player && hasDirty(living, PiSyncRoute.OWNER)) {
            CompoundTag ownerPayload = buildPayload(living, PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER, true);
            if (hasServices(ownerPayload)) {
                netService.sendToPlayer(player, CHANNEL, new PiLivingSyncPacket(living.getId(), PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER, ownerPayload));
                ownerSent = true;
            }
        }
        if (hasDirty(living, PiSyncRoute.TRACKING)) {
            CompoundTag trackingPayload = buildPayload(living, PiSyncEnvelopeKind.DELTA, PiSyncRoute.TRACKING, true);
            if (hasServices(trackingPayload)) {
                netService.sendToTracking(living, CHANNEL, new PiLivingSyncPacket(living.getId(), PiSyncEnvelopeKind.DELTA, PiSyncRoute.TRACKING, trackingPayload));
                trackingSent = true;
            }
        }
        if (ownerSent) {
            clearDirty(living, PiSyncRoute.OWNER);
        }
        if (trackingSent) {
            clearDirty(living, PiSyncRoute.TRACKING);
        }
    }

    /**
     * Applies a decoded packet payload to the matching generated services on a living entity.
     *
     * @param living target living entity
     * @param packet decoded sync packet
     */
    public static void apply(LivingEntity living, PiLivingSyncPacket packet) {
        CompoundTag servicesTag = packet.payload().getCompound("services");
        for (String key : servicesTag.getAllKeys()) {
            var id = net.minecraft.resources.ResourceLocation.tryParse(key);
            if (id == null) {
                continue;
            }
            PiActiveLivingServiceRegistry.find(id)
                    .ifPresent(descriptor -> descriptor.applySyncPayload(living, packet.kind(), packet.route(), servicesTag.getCompound(key)));
        }
    }

    private static PiNetService ensureRegistered() {
        PiNetService netService = PiNetServices.find().orElse(null);
        if (netService == null || registered) {
            return netService;
        }
        synchronized (PiLivingSyncMessages.class) {
            if (!registered) {
                netService.registerPlayMessage(CHANNEL, PiLivingSyncPacket.class, CODEC, HANDLER);
                registered = true;
            }
        }
        return netService;
    }

    private static boolean hasServices(CompoundTag payload) {
        return !payload.getCompound("services").getAllKeys().isEmpty();
    }

    private static CompoundTag buildPayload(LivingEntity living, PiSyncEnvelopeKind kind, PiSyncRoute route, boolean deltaOnly) {
        CompoundTag payload = new CompoundTag();
        CompoundTag services = new CompoundTag();
        for (var descriptor : PiActiveLivingServiceRegistry.activeDescriptors()) {
            if (deltaOnly && !descriptor.hasDirty(living, route)) {
                continue;
            }
            CompoundTag serviceTag = descriptor.buildSyncPayload(living, kind, route);
            if (hasVisibleData(serviceTag)) {
                services.put(descriptor.id().toString(), serviceTag);
            }
        }
        payload.put("services", services);
        return payload;
    }

    private static boolean hasDirty(LivingEntity living, PiSyncRoute route) {
        for (var descriptor : PiActiveLivingServiceRegistry.activeDescriptors()) {
            if (descriptor.hasDirty(living, route)) {
                return true;
            }
        }
        return false;
    }

    private static void clearDirty(LivingEntity living, PiSyncRoute route) {
        for (var descriptor : PiActiveLivingServiceRegistry.activeDescriptors()) {
            descriptor.clearDirty(living, route);
        }
    }

    private static boolean hasVisibleData(CompoundTag tag) {
        for (String key : tag.getAllKeys()) {
            if (!key.startsWith("__pi_")) {
                return true;
            }
        }
        return false;
    }
}
