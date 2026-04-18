package org.pickaid.pibrary.runtime.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Clientbound sync packet consumer for generated living services.
 */
@OnlyIn(Dist.CLIENT)
public final class PiLivingSyncClient {
    private PiLivingSyncClient() {
    }

    /**
     * Applies a living-service sync packet immediately or queues it until the target entity exists.
     *
     * @param packet decoded living sync packet
     */
    public static void handle(PiLivingSyncPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            PiPendingLivingSyncs.enqueue(packet);
            return;
        }
        Entity entity = minecraft.level.getEntity(packet.entityId());
        if (!(entity instanceof LivingEntity living)) {
            PiPendingLivingSyncs.enqueue(packet);
            return;
        }
        PiPendingLivingSyncs.applyTo(living);
        PiLivingSyncMessages.apply(living, packet);
    }
}
