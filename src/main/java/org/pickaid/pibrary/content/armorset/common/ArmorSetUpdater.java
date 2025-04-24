package org.pickaid.pibrary.content.armorset.common;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.pickaid.pibrary.api.registry.ArmorSetRegistry;
import org.pickaid.pibrary.content.armorset.ArmorSet;
import org.pickaid.pibrary.content.armorset.ISetEffect;
import org.pickaid.pibrary.content.armorset.capability.ArmorSetCapability;
import org.pickaid.pibrary.network.PibraryNetworkHandler;

import java.util.Set;

/**
 * Class responsible for updating the armor set effects on entities.
 * <p>
 * Used in handlers.
 * @author M1hono
 */
public class ArmorSetUpdater {
    protected ArmorSetUpdater() {
        super();
    }

    /**
     * Updates the armor set effects on the given entity.
     *
     * @param entity The entity to update the armor set effects on.
     */
    public static void updateEntityISetEffect(LivingEntity entity) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(armorSetCap -> {
                ArmorSet currentSet = armorSetCap.getActiveSet();
                ArmorSet newMatchedSet = findMatchingArmorSet(player);

                if (currentSet != newMatchedSet) {
                    if (currentSet != null) {
                        removeArmorSet(player, currentSet);
                    }
                    armorSetCap.setActiveSet(newMatchedSet);
                    if (newMatchedSet != null) {
                        applyArmorSet(player, newMatchedSet);
                    }
                    PibraryNetworkHandler.sendArmorSetSync((ServerPlayer) player, armorSetCap);
                }
            });
        }
    }

    /**
     * Finds the matching armor set for the given entity based on the items it is wearing.
     *
     * @param entity The entity to find the matching armor set for.
     * @return The matching armor set, or the empty set if no match is found.
     */
    private static ArmorSet findMatchingArmorSet(LivingEntity entity) {
        boolean hasAnyEquipment = false;
        Set<ArmorSet> firstMatchingSets = null;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack itemStack = entity.getItemBySlot(slot);
            if (!itemStack.isEmpty()) {
                hasAnyEquipment = true;
                Set<ArmorSet> setsForItem = ArmorSetRegistry.getItemToSetIndex().get(itemStack.getItem());
                if (setsForItem != null && !setsForItem.isEmpty()) {
                    firstMatchingSets = setsForItem;
                    break;
                }
            }
        }

        if (!hasAnyEquipment || firstMatchingSets == null) {
            return ArmorSetRegistry.EMPTY_SET.get();
        }

        return firstMatchingSets.stream()
                .filter(set -> set.matches(entity))
                .findFirst()
                .orElse(ArmorSetRegistry.EMPTY_SET.get());
    }


    /**
     * Applies the effects of the given armor set to the entity.
     *
     * @param entity The entity to apply the armor set effects to.
     * @param armorSet The armor set whose effects are to be applied.
     */
    private static void applyArmorSet(LivingEntity entity, ArmorSet armorSet) {
        armorSet.applyEffectsAndAttributes(entity);
        ISetEffect effect = armorSet.getEffect();
        if (effect != null) {
            effect.applyEffect(entity);
        }
    }

    /**
     * Removes the effects of the given armor set from the entity.
     *
     * @param entity The entity to remove the armor set effects from.
     * @param armorSet The armor set whose effects are to be removed.
     */
    private static void removeArmorSet(LivingEntity entity, ArmorSet armorSet) {
        armorSet.removeEffectsAndAttributes(entity);
        ISetEffect effect = armorSet.getEffect();
        if (effect != null) {
            effect.removeEffect(entity);
        }
    }
}