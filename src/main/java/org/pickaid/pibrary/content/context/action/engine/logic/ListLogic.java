package org.pickaid.pibrary.content.context.action.engine.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.init.LibraryObjects;

import java.util.List;

public record ListLogic(List<ConfiguredEngine<?>> children)
		implements ConfiguredEngine<ListLogic> {

	public static final Codec<ListLogic> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.list(ConfiguredEngine.CODEC).fieldOf("children").forGetter(e -> e.children)
	).apply(i, ListLogic::new));

	@Override
	public EngineType<ListLogic> type() {
		return LibraryObjects.LIST.get();
	}

	@Override
	public void execute(EngineContext ctx) {
		for (var e : children) {
			ctx.execute(e);
		}
	}

}
