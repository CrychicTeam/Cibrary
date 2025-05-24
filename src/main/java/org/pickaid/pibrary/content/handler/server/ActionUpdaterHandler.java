package org.pickaid.pibrary.content.handler.server;

import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.init.LibraryRegistries;

@Mod.EventBusSubscriber(modid = Pibrary.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ActionUpdaterHandler {

    @SubscribeEvent
    public static void onReload(TagsUpdatedEvent event) {
        event.getRegistryAccess().registryOrThrow(LibraryRegistries.PROJECTILE)
                .holders().forEach(e -> e.get().verify(e.key().location()));
        event.getRegistryAccess().registryOrThrow(LibraryRegistries.ACTION)
                .holders().forEach(e -> e.get().verify(e.key().location()));
    }
}
