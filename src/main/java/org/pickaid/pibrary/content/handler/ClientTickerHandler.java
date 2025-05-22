package org.pickaid.pibrary.content.handler;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.content.logic.ticker.BaseTicker;
import org.pickaid.pibrary.tools.utils.raytrace.EntityTarget;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientTickerHandler {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            BaseTicker.processTasks(true);
            for (EntityTarget target : EntityTarget.LIST) {
                target.tickRender();
            }
        }
    }
}