package org.pickaid.pibrary.content.context.action.engine.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.BuilderContext;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.variable.IntVariable;
import org.pickaid.pibrary.init.LibraryRegistries;

public record DelayLogic(IntVariable tick, ConfiguredEngine<?> child)
		implements ConfiguredEngine<DelayLogic> {

	public static Codec<DelayLogic> CODEC = RecordCodecBuilder.create(i -> i.group(
			IntVariable.codec("tick", DelayLogic::tick),
			ConfiguredEngine.codec("child", DelayLogic::child)
	).apply(i, DelayLogic::new));

	@Override
	public EngineType<DelayLogic> type() {
		return LibraryRegistries.DELAY.get();
	}

	@Override
	public void execute(EngineContext ctx) {
		ctx.schedule(tick.eval(ctx), () -> ctx.execute(child));
	}

	@Override
	public boolean verify(BuilderContext ctx) {
		return ConfiguredEngine.super.verify(ctx) & ctx.requiresScheduler();
	}
}
