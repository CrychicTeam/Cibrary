package org.crychicteam.cibrary.armorset.integration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.crychicteam.cibrary.Cibrary;
import org.crychicteam.cibrary.armorset.ArmorSetManager;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Cibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CuriosIntegration {
    private static boolean isCuriosLoaded = ModList.get().isLoaded("curios");
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

    public static boolean isPresent(LivingEntity entity, Item item) {
        if (!isCuriosLoaded) return false;
        List<ItemStack> allItems = getAllItems(entity);
        return allItems.stream().anyMatch(stack -> stack.getItem() == item);
    }

    public static Map<String, Integer> getEquippedCuriosTypesAndCounts(LivingEntity entity) {
        if (!isCuriosLoaded) return Collections.emptyMap();
        Map<String, Integer> curiosCounts = new HashMap<>();
        try {
            Object curioInventory = getCuriosInventoryMethod.invoke(null, entity);
            Method resolveMethod = curioInventory.getClass().getMethod("resolve");
            Optional<?> resolvedInventory = (Optional<?>) resolveMethod.invoke(curioInventory);
            if (resolvedInventory.isPresent()) {
                Object handler = resolvedInventory.get();
                Method getCuriosMethod = handler.getClass().getMethod("getCurios");
                Map<String, ?> curios = (Map<String, ?>) getCuriosMethod.invoke(handler);
                for (Map.Entry<String, ?> entry : curios.entrySet()) {
                    String slotType = entry.getKey();
                    int count = (int) streamStacks(entry.getValue()).filter(stack -> !stack.isEmpty()).count();
                    if (count > 0) {
                        curiosCounts.put(slotType, count);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curiosCounts;
    }

    public static boolean matchesCurioRequirements(LivingEntity entity, Map<String, Integer> requiredCurios) {
        if (!isCuriosLoaded) return true;
        List<ItemStack> equippedCurios = getAllItems(entity);
        Map<Item, Integer> equippedCuriosCount = new HashMap<>();
        for (ItemStack stack : equippedCurios) {
            equippedCuriosCount.merge(stack.getItem(), 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : requiredCurios.entrySet()) {
            String itemId = entry.getKey();
            int requiredCount = entry.getValue();
            Item requiredItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (requiredItem == null) {
                Cibrary.LOGGER.warn("Required curio item not found in registry: {}. Skipping this requirement.", itemId);
                continue;
            }

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
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    ArmorSetManager armorSetManager = Cibrary.ARMOR_SET_MANAGER;
                    if (armorSetManager != null) {
                        armorSetManager.updateEntitySetEffect(livingEntity);
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