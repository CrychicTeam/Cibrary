package org.pickaid.pibrary.content.context.action.engine.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.action.engine.core.EntityProcessor;
import org.pickaid.pibrary.content.context.action.engine.core.EntitySelector;
import org.pickaid.pibrary.content.context.action.engine.selector.SelectionType;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public record ProcessorEngine(
		SelectionType target,
		EntitySelector<?> selector,
		List<EntityProcessor<?>> processors
) implements ConfiguredEngine<ProcessorEngine> {

	public static final Codec<ProcessorEngine> CODEC = RecordCodecBuilder.create(i -> i.group(
			SelectionType.CODEC.fieldOf("target").forGetter(e -> e.target),
			EntitySelector.CODEC.fieldOf("selector").forGetter(e -> e.selector),
			Codec.list(EntityProcessor.CODEC).fieldOf("processors").forGetter(e -> e.processors)
	).apply(i, ProcessorEngine::new));

	@Override
	public EngineType<ProcessorEngine> type() {
		return LibraryRegistries.PROCESS_ENGINE.get();
	}

	@Override
	public void execute(EngineContext ctx) {
		if (!(ctx.user().level() instanceof ServerLevel sl)) return;
		var set = selector().find(sl, ctx, target);
		for (var p : processors()) {
			p.process(set, ctx);
		}
	}

}
