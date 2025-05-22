package org.pickaid.pibrary.network.util;import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.pickaid.pibrary.tools.utils.raytrace.RayTraceUtils;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public class TargetSetPacket {
    public UUID player;
    public UUID target;

    public TargetSetPacket(UUID player, @Nullable UUID target) {
        this.player = player;
        this.target = target;
    }


    public static void encode(TargetSetPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.player);
        buffer.writeBoolean(packet.target != null);
        if (packet.target != null) {
            buffer.writeUUID(packet.target);
        }
    }

    public static TargetSetPacket decode(FriendlyByteBuf buffer) {
        UUID player = buffer.readUUID();
        UUID target = null;
        if (buffer.readBoolean()) {
            target = buffer.readUUID();
        }
        return new TargetSetPacket(player, target);
    }

    public static void handle(TargetSetPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            RayTraceUtils.sync(packet);
        });
        context.setPacketHandled(true);
    }
}