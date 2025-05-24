package org.pickaid.pibrary;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.pickaid.pibrary.content.context.action.entity.renderer.PirojectileRenderer;
import org.pickaid.pibrary.content.context.action.particle.core.PiGenericParticleProvider;
import org.pickaid.pibrary.init.LibraryObjects;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PibraryClient {

    @SubscribeEvent
    public static void onParticleRegistryEvent(RegisterParticleProvidersEvent event) {
        event.registerSpecial(LibraryObjects.GENERIC_PARTICLE.get(), new PiGenericParticleProvider());
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
        });
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(LibraryObjects.GENERIC_PROJECTILE.get(), PirojectileRenderer::new);
    }

    @SubscribeEvent
    public static void registerItemDecoration(RegisterItemDecorationsEvent event) {

    }


    @SubscribeEvent
    public static void onResourceReload(RegisterClientReloadListenersEvent event) {

    }

}
