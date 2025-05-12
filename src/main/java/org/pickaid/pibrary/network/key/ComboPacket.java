package org.pickaid.pibrary.network.key;

import dev.xkmc.l2serial.network.SerialPacketBase;
import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.event.ComboTriggeredEvent;

@SerialClass
public class ComboPacket extends SerialPacketBase {
    
    @SerialClass.SerialField
    public ResourceLocation comboId;
    
    public ComboPacket() {}
    
    public ComboPacket(ResourceLocation comboId) {
        this.comboId = comboId;
    }
    
    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                ServerPlayer player = context.getSender();
                if (player != null && comboId != null) {
                    MinecraftForge.EVENT_BUS.post(new ComboTriggeredEvent(player, comboId));
                }
            }
        });
    }
}
