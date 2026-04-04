package org.pickaid.pibrary.runtime.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class PiLivingSyncClient {
    private PiLivingSyncClient() {
    }

    public static void handle(PiLivingSyncPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(packet.entityId());
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        PiLivingSyncMessages.apply(living, packet);
    }
}
