package org.pickaid.pibrary.content.context.action.entity.motion;

import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.content.context.action.entity.core.Motion;
import net.minecraft.world.phys.Vec3;
import org.pickaid.pibrary.api.fastprojectileapi.entity.ProjectileMovement;

public interface SetPosMotion<T extends Record & SetPosMotion<T>> extends Motion<T> {

	@Override
	default ProjectileMovement move(EngineContext ctx, Vec3 vec, Vec3 pos) {
		//ctx = ctx.with(ctx.loc().with(pos));
		var old = ctx.loc();
		ctx = ctx.with(move(ctx));
		var diff = ctx.loc().pos().subtract(pos);
		return new ProjectileMovement(diff, ProjectileMovement.of(ctx.loc().dir()).rot());
	}

	LocationContext move(EngineContext ctx);
}
