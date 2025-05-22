package org.pickaid.pibrary.content.context.action.entity.motion;

import org.pickaid.pibrary.api.fastprojectileapi.entity.ProjectileMovement;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.content.context.action.entity.core.Motion;
import net.minecraft.world.phys.Vec3;

public interface SetDeltaMotion<T extends Record & SetDeltaMotion<T>> extends Motion<T> {

	@Override
	default ProjectileMovement move(EngineContext ctx, Vec3 vec, Vec3 pos) {
		ctx = ctx.with(ctx.loc().with(vec));
		ctx = ctx.with(move(ctx));
		return new ProjectileMovement(ctx.loc().pos(), ProjectileMovement.of(ctx.loc().dir()).rot());
	}

	LocationContext move(EngineContext ctx);


}
