package org.pickaid.pibrary.content.handler;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.content.logic.ticker.BaseTicker;
import org.pickaid.pibrary.tools.utils.raytrace.RayTraceUtils;

@Mod.EventBusSubscriber
public class ServerTickerHandler {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            BaseTicker.processTasks(false);
            RayTraceUtils.serverTick();
        }
    }
}