package org.pickaid.pibrary.content.context.action.engine.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.variable.BooleanVariable;
import org.pickaid.pibrary.init.LibraryObjects;

import javax.annotation.Nullable;

public record PredicateLogic(
		BooleanVariable predicate,
		@Nullable ConfiguredEngine<?> action,
		@Nullable ConfiguredEngine<?> fallback
) implements ConfiguredEngine<PredicateLogic> {

	public static Codec<PredicateLogic> CODEC = RecordCodecBuilder.create(i -> i.group(
			BooleanVariable.CODEC.fieldOf("predicate").forGetter(e -> e.predicate),
			ConfiguredEngine.optionalCodec("action", PredicateLogic::action),
			ConfiguredEngine.optionalCodec("fallback", PredicateLogic::fallback)
	).apply(i, (a, b, c) -> new PredicateLogic(a, b.orElse(null), c.orElse(null))));

	@Override
	public EngineType<PredicateLogic> type() {
		return LibraryObjects.IF.get();
	}

	@Override
	public void execute(EngineContext ctx) {
		if (predicate.eval(ctx)) {
			if (action != null) ctx.execute(action);
		} else if (fallback != null) ctx.execute(fallback);
	}

}
