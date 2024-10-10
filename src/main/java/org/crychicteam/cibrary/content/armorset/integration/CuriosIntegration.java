package org.crychicteam.cibrary.content.armorset.integration;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
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
    private static Method findMethod;
    private static Method getEquippedCuriosMethod;
    private static Method getCuriosMethod;
    private static Method setEquippedCurioMethod;
    private static Method getSlotHelperMethod;

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
                findMethod = iDynamicStackHandlerClass.getMethod("find", Item.class);
                getEquippedCuriosMethod = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler").getMethod("getEquippedCurios");
                getCuriosMethod = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler").getMethod("getCurios");
                setEquippedCurioMethod = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler").getMethod("setEquippedCurio", String.class, int.class, ItemStack.class);
                getSlotHelperMethod = curiosApiClass.getMethod("getSlotHelper");
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

    public static int getSlot(LivingEntity entity, Item item) {
        if (!isCuriosLoaded) return -1;
        try {
            Object curioInventory = getCuriosInventoryMethod.invoke(null, entity);
            Method resolveMethod = curioInventory.getClass().getMethod("resolve");
            Optional<?> resolvedInventory = (Optional<?>) resolveMethod.invoke(curioInventory);
            if (resolvedInventory.isPresent()) {
                Object handler = resolvedInventory.get();
                Object equippedCurios = getEquippedCuriosMethod.invoke(handler);
                return (int) findMethod.invoke(equippedCurios, item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean isPresent(LivingEntity entity, Item item) {
        return getSlot(entity, item) != -1;
    }

    public static int getSlotByIdentifier(LivingEntity entity, Item item, String identifier) {
        if (!isCuriosLoaded) return -1;
        try {
            Object curioInventory = getCuriosInventoryMethod.invoke(null, entity);
            Method resolveMethod = curioInventory.getClass().getMethod("resolve");
            Optional<?> resolvedInventory = (Optional<?>) resolveMethod.invoke(curioInventory);
            if (resolvedInventory.isPresent()) {
                Object handler = resolvedInventory.get();
                Map<String, ?> curios = (Map<String, ?>) getCuriosMethod.invoke(handler);
                Object stacksHandler = curios.get(identifier);
                if (stacksHandler != null) {
                    Object stacks = getStacksMethod.invoke(stacksHandler);
                    return (int) findMethod.invoke(stacks, item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int getEmptySlotByIdentifier(LivingEntity entity, String identifier) {
        return getSlotByIdentifier(entity, Item.byId(0), identifier); // 0 is the ID for air
    }

    public static int getTotalSlotsByIdentifier(LivingEntity entity, String identifier) {
        if (!isCuriosLoaded) return 0;
        try {
            Object curioInventory = getCuriosInventoryMethod.invoke(null, entity);
            Method resolveMethod = curioInventory.getClass().getMethod("resolve");
            Optional<?> resolvedInventory = (Optional<?>) resolveMethod.invoke(curioInventory);
            if (resolvedInventory.isPresent()) {
                Object handler = resolvedInventory.get();
                Map<String, ?> curios = (Map<String, ?>) getCuriosMethod.invoke(handler);
                Object stacksHandler = curios.get(identifier);
                if (stacksHandler != null) {
                    Object stacks = getStacksMethod.invoke(stacksHandler);
                    return (int) getSlotsMethod.invoke(stacks);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static ItemStack getStackInSlotByIdentifier(LivingEntity entity, String identifier, int slot) {
        if (!isCuriosLoaded) return ItemStack.EMPTY;
        try {
            Object curioInventory = getCuriosInventoryMethod.invoke(null, entity);
            Method resolveMethod = curioInventory.getClass().getMethod("resolve");
            Optional<?> resolvedInventory = (Optional<?>) resolveMethod.invoke(curioInventory);
            if (resolvedInventory.isPresent()) {
                Object handler = resolvedInventory.get();
                Map<String, ?> curios = (Map<String, ?>) getCuriosMethod.invoke(handler);
                Object stacksHandler = curios.get(identifier);
                if (stacksHandler != null) {
                    Object stacks = getStacksMethod.invoke(stacksHandler);
                    return (ItemStack) getStackInSlotMethod.invoke(stacks, slot);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ItemStack.EMPTY;
    }

    public static void setStackInSlotByIdentifier(LivingEntity entity, String identifier, int slot, ItemStack itemStack) {
        if (!isCuriosLoaded) return;
        try {
            Object curioInventory = getCuriosInventoryMethod.invoke(null, entity);
            Method resolveMethod = curioInventory.getClass().getMethod("resolve");
            Optional<?> resolvedInventory = (Optional<?>) resolveMethod.invoke(curioInventory);
            if (resolvedInventory.isPresent()) {
                Object handler = resolvedInventory.get();
                setEquippedCurioMethod.invoke(handler, identifier, slot, itemStack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> getCurioInfo(LivingEntity entity, String id) {
        Map<String, Object> result = new HashMap<>();
        result.put("hasItem", false);
        result.put("count", 0);
        result.put("slots", new ArrayList<Integer>());

        List<ItemStack> curiosItems = getAllItems(entity);
        for (int i = 0; i < curiosItems.size(); i++) {
            ItemStack stack = curiosItems.get(i);
            if (!stack.isEmpty() && stack.getItem().toString().equals(id)) {
                result.put("hasItem", true);
                result.put("count", (int) result.get("count") + stack.getCount());
                ((List<Integer>) result.get("slots")).add(i);
            }
        }

        return result;
    }

    public static List<Map<String, Object>> getUniqueCuriosItems(LivingEntity entity) {
        List<ItemStack> curiosItems = getAllItems(entity);
        Map<String, Map<String, Object>> uniqueItems = new HashMap<>();

        for (ItemStack stack : curiosItems) {
            if (!stack.isEmpty()) {
                String id = stack.getItem().toString();
                uniqueItems.computeIfAbsent(id, k -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", id);
                    item.put("count", 0);
                    item.put("name", stack.getHoverName().getString());
                    return item;
                });
                uniqueItems.get(id).put("count", (int) uniqueItems.get(id).get("count") + stack.getCount());
            }
        }

        return new ArrayList<>(uniqueItems.values());
    }

    public static boolean isWearing(LivingEntity entity, Object item) {
        if (item instanceof String) {
            return (boolean) getCurioInfo(entity, (String) item).get("hasItem");
        } else if (item instanceof Item) {
            return isPresent(entity, (Item) item);
        }
        return false;
    }

    public static int getCurioItemCount(LivingEntity entity, String itemId) {
        return (int) getCurioInfo(entity, itemId).get("count");
    }

    public static Map<String, Object> getIdentifierInfo(LivingEntity entity, String identifier) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalSlots", getTotalSlotsByIdentifier(entity, identifier));
        result.put("emptySlot", getEmptySlotByIdentifier(entity, identifier));
        return result;
    }

    public static Map<String, Boolean> areItemsPresent(LivingEntity entity, List<String> itemIds) {
        Map<String, Boolean> result = new HashMap<>();
        for (String id : itemIds) {
            result.put(id, isWearing(entity, id));
        }
        return result;
    }

    public static ItemStack safeGetCurioStack(LivingEntity entity, String identifier, int slot) {
        try {
            return getStackInSlotByIdentifier(entity, identifier, slot);
        } catch (Exception e) {
            e.printStackTrace();
            return ItemStack.EMPTY;
        }
    }

    public static void slotOperation(String method, String slot, LivingEntity entity, int amount) {
        if (!isCuriosLoaded) return;
        try {
            Object slotHelper = getSlotHelperMethod.invoke(null);
            Method operationMethod;
            switch (method) {
                case "shrink":
                    operationMethod = slotHelper.getClass().getMethod("shrinkSlotType", String.class, int.class, LivingEntity.class);
                    operationMethod.invoke(slotHelper, slot, amount, entity);
                    break;
                case "grow":
                    operationMethod = slotHelper.getClass().getMethod("growSlotType", String.class, int.class, LivingEntity.class);
                    operationMethod.invoke(slotHelper, slot, amount, entity);
                    break;
                case "getfor":
                    operationMethod = slotHelper.getClass().getMethod("getSlotsForType", LivingEntity.class, String.class);
                    int result = (int) operationMethod.invoke(slotHelper, entity, slot);
                    // Handle the result as needed
                    break;
                case "setfor":
                    operationMethod = slotHelper.getClass().getMethod("setSlotsForType", String.class, LivingEntity.class, int.class);
                    operationMethod.invoke(slotHelper, slot, entity, amount);
                    break;
                case "unlock":
                    operationMethod = slotHelper.getClass().getMethod("unlockSlotType", String.class, LivingEntity.class);
                    operationMethod.invoke(slotHelper, slot, entity);
                    break;
                case "lock":
                    operationMethod = slotHelper.getClass().getMethod("lockSlotType", String.class, LivingEntity.class);
                    operationMethod.invoke(slotHelper, slot, entity);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid method: " + method);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}