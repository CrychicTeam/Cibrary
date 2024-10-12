package org.crychicteam.cibrary.content.events.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.crychicteam.cibrary.Cibrary;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Cibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {
//    @SubscribeEvent
//    public static void onItemTooltip(ItemTooltipEvent event) {
//        ItemStack itemStack = event.getItemStack();
//        Player player = event.getEntity();
//        if (player != null) {
//            player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(cap -> {
//                var additionalTooltip = cap.getAdditionalTooltip(itemStack);
//                if (!additionalTooltip.isEmpty()) {
//                    event.getToolTip().addAll(additionalTooltip);
//                }
//            });
//        }
//    }

    /**
     * Tooltip logic has been finalized, but, its format still needs design.
     * It will be an interface or implement in Armorset class.
     */
}
