package org.pickaid.pibrary.content.events.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.registry.ArmorSetRegistry;
import org.pickaid.pibrary.content.armorset.ArmorSet;
import org.pickaid.pibrary.content.armorset.ISetEffect;
import org.pickaid.pibrary.content.armorset.common.ArmorSetManager;

/**
 * Handles tooltips for armor set items.
 * Displays information about active armor sets and their effects when hovering over items.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {

    private static final Component EMPTY_LINE = Component.empty();
    private static final String ARMOR_SET_TRANSLATION_KEY = "tooltip.pibrary.armor_set";
    private static final String ARMOR_SET_EFFECT_TRANSLATION_KEY = "tooltip.pibrary.armor_set.effect";
    private static final String SET_EFFECT_PREFIX = "set_effect.";

    /**
     * Event handler for item tooltips.
     * Adds armor set information to tooltips when applicable.
     *
     * @param event The tooltip event
     */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        Player player = event.getEntity();

        if (player == null) {
            return;
        }
        Item item = itemStack.getItem();
        if (!ArmorSetManager.isItemInActiveSet(player, item)) {
            return;
        }
        ArmorSet activeSet = ArmorSetManager.getActiveArmorSet(player);
        EquipmentSlot equipmentSlot = itemStack.getEquipmentSlot();

        if (equipmentSlot != null) {
            addEquipmentTooltips(event, player, itemStack, activeSet);
        } else if (isCurioItem(item, activeSet)) {
            addCurioTooltips(event, player, itemStack, activeSet);
        } else {
            addGenericTooltips(event, player, itemStack, activeSet);
        }
    }

    private static void addEquipmentTooltips(ItemTooltipEvent event, Player player, ItemStack itemStack, ArmorSet set) {
        ISetEffect effect = set.getEffect();
        if (effect == null) {
            return;
        }

        ResourceLocation setId = ArmorSetRegistry.getRegistry().getKey(set);
        if (setId != null) {
            event.getToolTip().add(
                    Component.translatable(ARMOR_SET_TRANSLATION_KEY)
                            .append(Component.translatable(setId.toLanguageKey()))
                            .withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD)
            );

            event.getToolTip().add(set.getDescription(player, itemStack));
            event.getToolTip().add(EMPTY_LINE);
            event.getToolTip().add(
                    Component.translatable(ARMOR_SET_EFFECT_TRANSLATION_KEY)
                            .append(Component.translatable(SET_EFFECT_PREFIX + effect.getIdentifier()))
                            .withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD)
            );
            event.getToolTip().add(effect.getDescription(player, itemStack));
        }
    }

    private static void addCurioTooltips(ItemTooltipEvent event, Player player, ItemStack itemStack, ArmorSet set) {
        if (set.checkDescription(player, itemStack)) {
            event.getToolTip().add(set.getDescription(player, itemStack));
        }
        ISetEffect effect = set.getEffect();
        if (effect != null && effect.checkDescription(player, itemStack)) {
            event.getToolTip().add(effect.getDescription(player, itemStack));
        }
    }

    private static void addGenericTooltips(ItemTooltipEvent event, Player player, ItemStack itemStack, ArmorSet set) {
        if (set.checkDescription(player, itemStack)) {
            event.getToolTip().add(set.getDescription(player, itemStack));
        }
        ISetEffect effect = set.getEffect();
        if (effect != null && effect.checkDescription(player, itemStack)) {
            event.getToolTip().add(effect.getDescription(player, itemStack));
        }
    }

    private static boolean isCurioItem(Item item, ArmorSet set) {
        return set.getCurioItems().containsKey(item);
    }
}