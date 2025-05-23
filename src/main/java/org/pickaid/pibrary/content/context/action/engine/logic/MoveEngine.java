package org.pickaid.pibrary.content.context.action.engine.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.action.engine.core.Modifier;
import org.pickaid.pibrary.init.LibraryObjects;

import java.util.List;

public record MoveEngine(List<Modifier<?>> modifiers, ConfiguredEngine<?> child)
		implements ConfiguredEngine<MoveEngine> {

	public static final Codec<MoveEngine> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.list(Modifier.CODEC).fieldOf("modifiers").forGetter(e -> e.modifiers),
			ConfiguredEngine.codec("child", e -> e.child)
	).apply(i, MoveEngine::new));

	@Override
	public EngineType<MoveEngine> type() {
		return LibraryObjects.MOVE_ENGINE.get();
	}

	@Override
	public void execute(EngineContext ctx) {
		for (var e : modifiers) {
			ctx = ctx.with(e.modify(ctx));
		}
		ctx.execute(child);
	}

}
