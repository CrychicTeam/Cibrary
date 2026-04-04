package org.pickaid.pibrary.runtime.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.pickaid.pinet.api.channel.PiChannelId;
import org.pickaid.pinet.api.message.PiMessageCodec;
import org.pickaid.pinet.api.message.PiMessageHandler;
import org.pickaid.pinet.api.service.PiNetService;
import org.pickaid.pinet.api.service.PiNetServices;
import org.pickaid.pinet.api.sync.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.PiSyncRoute;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.runtime.service.PiLivingServiceDescriptors;

public final class PiLivingSyncMessages {
    private static final PiChannelId CHANNEL = new PiChannelId(Pibrary.id("living_services"));
    private static final PiMessageCodec<PiLivingSyncPacket> CODEC = new PiMessageCodec<>() {
        @Override
        public void encode(FriendlyByteBuf buffer, PiLivingSyncPacket value) {
            buffer.writeVarInt(value.entityId());
            buffer.writeEnum(value.kind());
            buffer.writeEnum(value.route());
            buffer.writeNbt(value.payload());
        }

        @Override
        public PiLivingSyncPacket decode(FriendlyByteBuf buffer) {
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

    public static void bootstrap() {
        ensureRegistered();
    }

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

    public static void apply(LivingEntity living, PiLivingSyncPacket packet) {
        CompoundTag servicesTag = packet.payload().getCompound("services");
        for (String key : servicesTag.getAllKeys()) {
            var id = net.minecraft.resources.ResourceLocation.tryParse(key);
            if (id == null) {
                continue;
            }
            PiLivingServiceDescriptors.findGenerated(id)
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
        for (var descriptor : PiLivingServiceDescriptors.generatedDescriptors()) {
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
        for (var descriptor : PiLivingServiceDescriptors.generatedDescriptors()) {
            if (descriptor.hasDirty(living, route)) {
                return true;
            }
        }
        return false;
    }

    private static void clearDirty(LivingEntity living, PiSyncRoute route) {
        for (var descriptor : PiLivingServiceDescriptors.generatedDescriptors()) {
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
