package org.pickaid.pibrary.network.capability;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.pickaid.pibrary.api.core.capability.PlayerCapabilityHolder;
import org.pickaid.pibrary.api.core.capability.PlayerCapabilityTemplate;

import java.util.function.Supplier;

public class CapabilitySyncPacket {

    public enum Action {
        ALL,
        CLONE,
        TRACK,
        UPDATE
    }

    private final Action action;
    private final ResourceLocation capabilityId;
    private final CompoundTag data;
    private final int targetPlayerId;

    public CapabilitySyncPacket(Action action, ResourceLocation capabilityId, CompoundTag data) {
        this(action, capabilityId, data, -1);
    }

    public CapabilitySyncPacket(Action action, ResourceLocation capabilityId, CompoundTag data, int targetPlayerId) {
        this.action = action;
        this.capabilityId = capabilityId;
        this.data = data;
        this.targetPlayerId = targetPlayerId;
    }

    public static void encode(CapabilitySyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
        buffer.writeResourceLocation(packet.capabilityId);
        buffer.writeNbt(packet.data);
        buffer.writeInt(packet.targetPlayerId);
    }

    public static CapabilitySyncPacket decode(FriendlyByteBuf buffer) {
        Action action = buffer.readEnum(Action.class);
        ResourceLocation capabilityId = buffer.readResourceLocation();
        CompoundTag data = buffer.readNbt();
        int targetPlayerId = buffer.readInt();

        return new CapabilitySyncPacket(action, capabilityId, data, targetPlayerId);
    }

    public static void handle(CapabilitySyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleClientSide(packet);
        });
        context.setPacketHandled(true);
    }

    private static void handleClientSide(CapabilitySyncPacket packet) {
        PlayerCapabilityHolder<?> holder = PlayerCapabilityHolder.getPlayerCapability(packet.capabilityId);
        if (holder == null) return;

        Player targetPlayer = getTargetPlayer(packet);
        if (targetPlayer == null) return;

        PlayerCapabilityTemplate<?> capability = holder.get(targetPlayer);
        if (capability == null) return;

        switch (packet.action) {
            case ALL:
            case CLONE:
                capability.preInject();
                capability.loadFromNBT(packet.data);
                capability.init();
                break;

            case TRACK:
            case UPDATE:
                capability.preInject();
                capability.deserializeFromSync(packet.data);
                capability.init();
                break;
        }
    }

    private static Player getTargetPlayer(CapabilitySyncPacket packet) {
        if (packet.targetPlayerId == -1) {
            return Minecraft.getInstance().player;
        } else {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                return (Player) level.getEntity(packet.targetPlayerId);
            }
        }
        return null;
    }
}