package org.pickaid.pibrary.content.context.action.engine.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.init.LibraryRegistries;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public record RandomVariableLogic(String name, int count, ConfiguredEngine<?> child)
		implements ConfiguredEngine<RandomVariableLogic> {

	public static Codec<RandomVariableLogic> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.fieldOf("name").forGetter(e -> e.name),
			Codec.INT.fieldOf("count").forGetter(e -> e.count),
			ConfiguredEngine.codec("child", RandomVariableLogic::child)
	).apply(i, RandomVariableLogic::new));

	@Override
	public EngineType<RandomVariableLogic> type() {
		return LibraryRegistries.RANDOM.get();
	}

	@Override
	public void execute(EngineContext ctx) {
		var params = new HashMap<>(ctx.parameters());
		for (int i = 0; i < count; i++) {
			params.put(name + i, ctx.rand().nextDouble());
		}
		ctx.execute(ctx.loc(), params, child);
	}

	public Set<String> verificationParameters() {
		Set<String> ans = new LinkedHashSet<>();
		for (int i = 0; i < count; i++) {
			ans.add(name + i);
		}
		return ans;
	}

}
