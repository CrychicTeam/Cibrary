package org.pickaid.pibrary.network.key;

import dev.xkmc.l2serial.network.SerialPacketBase;
import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraftforge.network.NetworkEvent;
import org.pickaid.pibrary.content.events.client.ClientKeyHandler;
import org.pickaid.pibrary.content.key.KeyData;

@SerialClass
public class SetKeyStatePacket extends SerialPacketBase {
    @SerialClass.SerialField
    public KeyData keyData;

    public SetKeyStatePacket() {}

    public SetKeyStatePacket(KeyData keyData) {
        this.keyData = keyData;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientKeyHandler.setKeyState(keyData);
            }
        });
    }
}