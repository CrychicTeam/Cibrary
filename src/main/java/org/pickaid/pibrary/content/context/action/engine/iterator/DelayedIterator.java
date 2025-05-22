package org.pickaid.pibrary.content.context.action.engine.iterator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.BuilderContext;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.variable.IntVariable;
import org.pickaid.pibrary.init.LibraryRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

public record DelayedIterator(IntVariable step, IntVariable delay, ConfiguredEngine<?> child, @Nullable String index)
		implements Iterator<DelayedIterator> {

	public static Codec<DelayedIterator> CODEC = RecordCodecBuilder.create(i -> i.group(
			IntVariable.codec("step", DelayedIterator::step),
			IntVariable.codec("delay", DelayedIterator::delay),
			ConfiguredEngine.codec("child", Iterator::child),
			Codec.STRING.optionalFieldOf("index").forGetter(e -> Optional.ofNullable(e.index))
	).apply(i, (d, e, f, g) -> new DelayedIterator(d, e, f, g.orElse(null))));

	@Override
	public EngineType<DelayedIterator> type() {
		return LibraryRegistries.ITERATE_DELAY.get();
	}

	@Override
	public void execute(EngineContext ctx) {
		int step = step().eval(ctx);
		int delay = delay().eval(ctx);
		for (int i = 0; i < step; i++) {
			int I = i;
			if (i == 0) ctx.iterateOn(ctx.loc(), index, 0, child);
			else ctx.schedule(i * delay, () -> ctx.iterateOn(ctx.loc(), index, I, child));
		}
	}

	@Override
	public boolean verify(BuilderContext ctx) {
		return Iterator.super.verify(ctx) & ctx.requiresScheduler();
	}
}
