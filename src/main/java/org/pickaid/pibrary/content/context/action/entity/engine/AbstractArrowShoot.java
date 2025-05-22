package org.pickaid.pibrary.content.context.action.entity.engine;

import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.pickaid.pibrary.api.fastprojectileapi.entity.ProjectileMovement;

public interface AbstractArrowShoot<T extends Record & ShootProjectileInstance<T>> extends ShootProjectileInstance<T> {

	AbstractArrow arrow(EngineContext ctx);

	DoubleVariable speed();

	@Override
	default Entity create(EngineContext ctx) {
		var ans = arrow(ctx);
		ans.moveTo(ctx.loc().pos());
		double v = speed().eval(ctx);
		var mov = ProjectileMovement.of(ctx.loc().dir().scale(v));
		ans.setDeltaMovement(mov.vec());
		ans.setXRot((float) (mov.rot().x * -Mth.RAD_TO_DEG));
		ans.setYRot((float) (mov.rot().y * -Mth.RAD_TO_DEG));
		ans.getPersistentData().putInt("DespawnFactor", 20);
		ans.pickup = ctx.user().user() instanceof Player ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.DISALLOWED;
		return ans;
	}

}
