package org.pickaid.pibrary.content.context.action.engine.particle;

import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import net.minecraft.core.particles.ParticleOptions;

import javax.annotation.Nullable;

public interface ParticleInstance<T extends Record & ParticleInstance<T>> extends ConfiguredEngine<T> {

	@Nullable
	ParticleOptions particle(EngineContext ctx);

	DoubleVariable speed();

	@Override
	default void execute(EngineContext ctx) {
		if (!ctx.user().level().isClientSide()) return;
		var opt = particle(ctx);
		if (opt == null) return;
		var pos = ctx.loc().pos();
		var vec = ctx.loc().dir().scale(speed().eval(ctx));
		ctx.user().level().addAlwaysVisibleParticle(opt, true, pos.x, pos.y, pos.z, vec.x, vec.y, vec.z);
	}

}
