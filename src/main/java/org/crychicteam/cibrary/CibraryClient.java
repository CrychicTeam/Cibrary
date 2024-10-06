package org.crychicteam.cibrary;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.crychicteam.cibrary.sound.CibrarySoundManagerHandler;

@Mod.EventBusSubscriber(modid = Cibrary.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CibraryClient {
    public static CibrarySoundManagerHandler CLIENT_SOUND_MANAGER;
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CLIENT_SOUND_MANAGER = CibrarySoundManagerHandler.getInstance();
    }
}
