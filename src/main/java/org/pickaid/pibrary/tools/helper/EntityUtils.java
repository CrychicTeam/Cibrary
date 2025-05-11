package org.pickaid.pibrary.tools.helper;

import com.endertech.minecraft.forge.math.AABBHelper;
import com.endertech.minecraft.forge.math.Vect3d;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import org.pickaid.pibrary.tools.math.GameUtils;

import java.util.Optional;

public class EntityUtils {
    public static boolean isEating(LivingEntity living) {
        if (living.isUsingItem()) {
            ItemStack item = living.getItemInHand(living.getUsedItemHand());
            return item.isEdible();
        } else {
            return false;
        }
    }

    public static boolean isEnemy(Entity entity) {
        return entity instanceof Enemy;
    }

    public static boolean isHostileMob(Entity entity) {
        return !entity.getType().getCategory().isFriendly();
    }

    public static Vect3d getCenteredPosTo(BlockPos pos) {
        return LevelReaderUtils.getBlockCenter(pos).withY((double)pos.getY());
    }

    public static Vect3d getCenterPosition(Entity entity) {
        return GameUtils.getBBCenter(getBB(entity));
    }

    public static InteractionHand otherHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    public static Optional<Entity> getById(Level level, int id) {
        return Optional.ofNullable(level.getEntity(id));
    }

    public static ResourceLocation getRegistryName(Entity entity) {
        return ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
    }

    public static Vect3d getMotion(Entity entity) {
        return Vect3d.from(entity.getDeltaMovement());
    }

    public static void setMotion(Entity entity, Vect3d vec) {
        entity.setDeltaMovement(vec.x, vec.y, vec.z);
    }

    public static void setPlayerMotion(Player player, Vect3d vec) {
        player.setDeltaMovement(vec.x, vec.y, vec.z);
        if (!player.level().isClientSide()) player.hurtMarked = true;
    }

    public static Vect3d getCurPosition(Entity entity) {
        return entity != null ? Vect3d.from(entity.getX(), entity.getY(), entity.getZ()) : Vect3d.ZERO;
    }

    public static AABB getBB(Entity entity) {
        return getBB(entity, getCurPosition(entity));
    }

    public static AABB getBB(Entity entity, Vect3d position) {
        if (entity != null && position != null) {
            double widthHalf = (double)entity.getBbWidth() / 2.0;
            return new AABB(position.x - widthHalf, position.y, position.z - widthHalf, position.x + widthHalf, position.y + (double)entity.getBbHeight(), position.z + widthHalf);
        } else {
            return AABBHelper.ZERO;
        }
    }
}