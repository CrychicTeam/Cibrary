package org.pickaid.pibrary.content.context.action.engine.context;

import org.pickaid.pibrary.content.context.action.engine.helper.Orientation;
import org.pickaid.pibrary.content.context.interaction.InteractionAction;
import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.pickaid.pibrary.tools.utils.raytrace.RayTraceUtils;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public record ActionContext(LivingEntity user, Vec3 origin, Orientation facing, long seed, double tickUsing,
							double power) {

	public static Set<String> DEFAULT_PARAMS = Set.of("TickUsing", "Power", "CastX", "CastY", "CastZ");

	public static Vec3 getCenter(LivingEntity le) {
		return le.position().add(0, le.getBbHeight() / 2f, 0);
	}

	public static Vec3 getForward(LivingEntity le) {
		if (le instanceof Player player) {
			return RayTraceUtils.getRayTerm(Vec3.ZERO, player.getXRot(), player.getYRot(), 1);
		}
		if (le instanceof Mob mob) {
			var target = mob.getTarget();
			if (target != null) {
				return getCenter(target).subtract(mob.getEyePosition()).normalize();
			}
		}
		return le.getForward();
	}

	@Nullable
	public static LivingEntity getTarget(LivingEntity le) {
		if (le instanceof Player player) {
			return RayTraceUtils.serverGetTarget(player);
		}
		if (le instanceof Mob mob) {
			return mob.getTarget();
		}
		return null;
	}

	@Nullable
	public static ActionContext castAction(LivingEntity user, InteractionAction action, int useTick, double power, int distance) {
		Level level = user.level();
		Vec3 pos;
		Orientation ori;
		switch (action.triggerType()) {
			case SELF_POS -> {
				pos = user.position();
				ori = Orientation.regular();
			}
			case TARGET_POS -> {
				var start = user.getEyePosition();
				var forward = ActionContext.getForward(user);
				var end = start.add(forward.scale(distance));
				AABB box = (new AABB(start, end)).inflate(1.0);
				var blockHitResult = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, user));
				var entityHitResult = ProjectileUtil.getEntityHitResult(level, user, start, end, box, e -> true);
				if ((entityHitResult == null || entityHitResult.getType() == HitResult.Type.MISS) && blockHitResult.getType() == HitResult.Type.MISS) {
					return null;
				}
				pos = entityHitResult != null && entityHitResult.getLocation().distanceToSqr(start) < blockHitResult.getLocation().distanceToSqr(start) ?
						entityHitResult.getLocation() : blockHitResult.getLocation();
				ori = Orientation.regular();
			}
			case HORIZONTAL_FACING -> {
				var dir = ActionContext.getForward(user).multiply(1, 0, 1).normalize();
				pos = user.position();
				if (dir.length() < 0.5) return null;
				ori = Orientation.fromForward(dir);
			}
			case FACING_BACK -> {
				var dir = ActionContext.getForward(user);
				pos = user.getEyePosition().add(dir.scale(-1));
				ori = Orientation.fromForward(dir);
			}
			case FACING_FRONT -> {
				var dir = ActionContext.getForward(user);
				pos = user.getEyePosition().add(dir);
				ori = Orientation.fromForward(dir);
			}
			case TARGET_ENTITY -> {
				var target = getTarget(user);
				if (target != null) {
					pos = target.position();
					ori = Orientation.regular();
				} else return null;
			}
			default -> {
				return null;
			}
		}
		long seed = 0;
		if (!level.isClientSide()) {
			seed = ThreadLocalRandom.current().nextLong();
		}
		return new ActionContext(user, pos, ori, seed, useTick, power);
	}

	public Map<String, Double> defaultArgs() {
		return Map.of("TickUsing", tickUsing(),
				"Power", power(),
				"CastX", origin().x,
				"CastY", origin().y,
				"CastZ", origin().z);
	}
}
