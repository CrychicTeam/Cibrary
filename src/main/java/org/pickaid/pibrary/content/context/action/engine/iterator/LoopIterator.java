package org.pickaid.pibrary.content.context.action.engine.iterator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.variable.IntVariable;
import org.pickaid.pibrary.init.LibraryRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

public record LoopIterator(IntVariable step, ConfiguredEngine<?> child, @Nullable String index)
		implements Iterator<LoopIterator> {

	public static Codec<LoopIterator> CODEC = RecordCodecBuilder.create(i -> i.group(
			IntVariable.codec("step", LoopIterator::step),
			ConfiguredEngine.codec("child", Iterator::child),
			Codec.STRING.optionalFieldOf("index").forGetter(e -> Optional.ofNullable(e.index))
	).apply(i, (d, f, g) -> new LoopIterator(d, f, g.orElse(null))));

	@Override
	public EngineType<LoopIterator> type() {
		return LibraryRegistries.ITERATE.get();
	}

	@Override
	public void execute(EngineContext ctx) {
		int step = step().eval(ctx);
		for (int i = 0; i < step; i++) {
			ctx.iterateOn(ctx.loc(), index, i, child);
		}
	}

}
