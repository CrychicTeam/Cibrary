package org.crychicteam.cibrary.content.armorset.integration;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import org.crychicteam.cibrary.Cibrary;
import org.crychicteam.cibrary.content.armorset.ArmorSetManager;
import org.crychicteam.cibrary.content.events.common.ArmorSetHandler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class CuriosIntegration {
    public static boolean isCuriosLoaded = ModList.get().isLoaded("curios");
    private static Class<?> curiosApiClass;
    private static Method getCuriosInventoryMethod;
    private static Method getStacksMethod;
    private static Method getSlotsMethod;
    private static Method getStackInSlotMethod;

    public static void init() {
        if (isCuriosLoaded) {
            try {
                curiosApiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                getCuriosInventoryMethod = curiosApiClass.getMethod("getCuriosInventory", LivingEntity.class);
                Class<?> iCurioStacksHandlerClass = Class.forName("top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler");
                getStacksMethod = iCurioStacksHandlerClass.getMethod("getStacks");
                Class<?> iDynamicStackHandlerClass = Class.forName("top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler");
                getSlotsMethod = iDynamicStackHandlerClass.getMethod("getSlots");
                getStackInSlotMethod = iDynamicStackHandlerClass.getMethod("getStackInSlot", int.class);
            } catch (Exception e) {
                e.printStackTrace();
                isCuriosLoaded = false;
            }
        }
    }

    public static List<ItemStack> getAllItems(LivingEntity entity) {
        if (!isCuriosLoaded) return Collections.emptyList();
        try {
            Object curioInventory = getCuriosInventoryMethod.invoke(null, entity);
            Method resolveMethod = curioInventory.getClass().getMethod("resolve");
            Optional<?> resolvedInventory = (Optional<?>) resolveMethod.invoke(curioInventory);
            if (resolvedInventory.isPresent()) {
                Object handler = resolvedInventory.get();
                Method getCuriosMethod = handler.getClass().getMethod("getCurios");
                Map<String, ?> curios = (Map<String, ?>) getCuriosMethod.invoke(handler);
                return curios.values().stream()
                        .flatMap(stacksHandler -> streamStacks(stacksHandler))
                        .filter(stack -> !stack.isEmpty())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static boolean matchesCurioRequirements(LivingEntity entity, Map<Item, Integer> requiredCurios) {
        if (!isCuriosLoaded) return true;
        List<ItemStack> equippedCurios = getAllItems(entity);
        Map<Item, Integer> equippedCuriosCount = new HashMap<>();
        for (ItemStack stack : equippedCurios) {
            equippedCuriosCount.merge(stack.getItem(), 1, Integer::sum);
        }
        for (Map.Entry<Item, Integer> entry : requiredCurios.entrySet()) {
            Item requiredItem = entry.getKey();
            int requiredCount = entry.getValue();

            int equippedCount = equippedCuriosCount.getOrDefault(requiredItem, 0);
            if (equippedCount < requiredCount) {
                return false;
            }
        }
        return true;
    }

    @SubscribeEvent
    public void onGenericEvent(Event event) {
        if (event.getClass().getName().equals("top.theillusivec4.curios.api.event.CurioChangeEvent")) {
            try {
                Object entity = event.getClass().getMethod("getEntity").invoke(event);
                if (entity instanceof ServerPlayer player) {
                    ArmorSetManager armorSetManager = Cibrary.ARMOR_SET_MANAGER;
                    if (armorSetManager != null) {
                        armorSetManager.updateEntitySetEffect(player);
                        ArmorSetHandler.syncArmorSet(player);
                    } else {
                        System.err.println("ArmorSetManager is null in CuriosIntegration.onGenericEvent");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static java.util.stream.Stream<ItemStack> streamStacks(Object stacksHandler) {
        try {
            Object stacks = getStacksMethod.invoke(stacksHandler);
            int slots = (int) getSlotsMethod.invoke(stacks);
            List<ItemStack> itemStacks = new ArrayList<>();
            for (int i = 0; i < slots; i++) {
                itemStacks.add((ItemStack) getStackInSlotMethod.invoke(stacks, i));
            }
            return itemStacks.stream();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.stream.Stream.empty();
        }
    }
}