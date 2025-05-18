package org.pickaid.pibrary.content.context.action.entity.engine;

import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import net.minecraft.world.entity.Entity;

public interface ShootProjectileInstance<T extends Record & ShootProjectileInstance<T>> extends ConfiguredEngine<T> {

	Entity create(EngineContext ctx);

	@Override
	default void execute(EngineContext ctx) {
		if (ctx.user().level().isClientSide()) return;
		ctx.user().level().addFreshEntity(create(ctx));

	}

}
