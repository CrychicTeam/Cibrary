package org.pickaid.pibrary.network.key;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import org.pickaid.pibrary.api.event.ComboTriggeredEvent;

import java.util.function.Supplier;

public record ComboPacket(ResourceLocation comboId) {

    public ComboPacket {
    }

    public static void encode(ComboPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.comboId);
    }

    public static ComboPacket decode(FriendlyByteBuf buffer) {
        return new ComboPacket(buffer.readResourceLocation());
    }

    public static void handle(ComboPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                ServerPlayer player = context.getSender();
                if (player != null && packet.comboId != null) {
                    MinecraftForge.EVENT_BUS.post(new ComboTriggeredEvent(player, packet.comboId));
                }
            }
        });
        context.setPacketHandled(true);
    }
}