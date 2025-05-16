package org.pickaid.pibrary.network.key;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.pickaid.pibrary.content.handler.client.ClientKeyHandler;
import org.pickaid.pibrary.content.key.KeyData;

import java.util.function.Supplier;

public record SetKeyStatePacket(KeyData keyData) {

    public SetKeyStatePacket {}

    public static void encode(SetKeyStatePacket packet, FriendlyByteBuf buffer) {
        packet.keyData.encode(buffer);
    }

    public static SetKeyStatePacket decode(FriendlyByteBuf buffer) {
        return new SetKeyStatePacket(KeyData.decode(buffer));
    }

    public static void handle(SetKeyStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientKeyHandler.setKeyState(packet.keyData);
            }
        });
        context.setPacketHandled(true);
    }
}