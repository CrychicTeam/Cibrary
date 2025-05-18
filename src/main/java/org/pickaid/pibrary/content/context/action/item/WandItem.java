package org.pickaid.pibrary.content.context.action.item;

import dev.xkmc.l2library.util.raytrace.FastItem;
import dev.xkmc.l2library.util.raytrace.IGlowingTarget;
import dev.xkmc.l2library.util.raytrace.RayTraceUtil;
import org.pickaid.pibrary.content.context.action.engine.context.ActionContext;
import org.pickaid.pibrary.content.context.skill.Action;
import org.pickaid.pibrary.content.context.skill.ActionCastType;
import org.pickaid.pibrary.content.context.skill.ActionTriggerType;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class WandItem extends Item implements IGlowingTarget, FastItem {

	private static final String KEY = "Pibrary:skill";


	@Nullable
	public static ResourceLocation getSpellId(Level level, ItemStack stack) {
		var root = stack.getTag();
		if (stack.isEmpty() || root == null) return null;
		if (!root.contains(KEY, Tag.TAG_STRING)) return null;
		String id = root.getString(KEY);
		if (!ResourceLocation.isValidResourceLocation(id))
			return null;
		return new ResourceLocation(id);
	}

	@Nullable
	public static Action getSpell(Level level, ItemStack stack) {
		ResourceLocation id = getSpellId(level, stack);
		if (id == null) return null;
		return level.registryAccess().registryOrThrow(LibraryRegistries.SPELL).get(id);
	}

	public static ItemStack setSpell(ItemStack stack, ResourceLocation id) {
		stack.getOrCreateTag().putString(KEY, id.toString());
		return stack;
	}

	public WandItem(Properties prop) {
		super(prop);
	}


	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		Action spell = getSpell(level, stack);
		if (spell != null) {
			if (spell.castType() == ActionCastType.INSTANT) {
				if (castSpell(stack, level, player, spell, 0, false)) {
					if (!level.isClientSide) {
						player.getCooldowns().addCooldown(this, 10);
					}
					return InteractionResultHolder.consume(stack);
				} else {
					return InteractionResultHolder.fail(stack);
				}
			} else {
				return ItemUtils.startUsingInstantly(level, player, hand);
			}
		}
		return super.use(level, player, hand);
	}

	@Override
	public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remain) {
		super.onUseTick(level, user, stack, remain);
		Action spell = getSpell(level, stack);
		if (spell != null) {
			if (spell.castType() == ActionCastType.CONTINUOUS) {
				castSpell(stack, level, user, spell, getUseDuration(stack) - remain, false);
			}
			if (spell.castType() == ActionCastType.CHARGE) {
				castSpell(stack, level, user, spell, getUseDuration(stack) - remain, true);
			}
		}
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remain) {
		Action spell = getSpell(level, stack);
		if (spell != null) {
			if (spell.castType() == ActionCastType.CHARGE) {
				castSpell(stack, level, user, spell, getUseDuration(stack) - remain, false);
			}
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (selected && entity instanceof Player player && level.isClientSide()) {
			Action spell = getSpell(level, stack);
			if (spell != null && spell.triggerType() == ActionTriggerType.TARGET_ENTITY) {
				RayTraceUtil.clientUpdateTarget(player, getDistance(stack));
			}
		}
	}

	private boolean castSpell(ItemStack stack, Level level, LivingEntity user, Action spell, int useTick, boolean charging) {
		ActionContext ctx = ActionContext.castSpell(user, spell, useTick, charging ? 0 : 1, getDistance(stack));
		if (ctx == null) return false;
		if (!level.isClientSide()) {
			spell.execute(ctx);
		}
		return true;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public int getDistance(ItemStack itemStack) {
		return 64;
	}

	@Override
	public boolean isFast(ItemStack itemStack) {
		return true;
	}

}
