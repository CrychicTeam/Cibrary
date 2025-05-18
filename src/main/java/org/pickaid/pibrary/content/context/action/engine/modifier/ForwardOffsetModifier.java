package org.pickaid.pibrary.content.context.action.engine.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.content.context.action.engine.core.Modifier;
import org.pickaid.pibrary.content.context.action.engine.core.ModifierType;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.init.LibraryRegistries;
public record ForwardOffsetModifier(DoubleVariable distance) implements Modifier<ForwardOffsetModifier> {

	public static Codec<ForwardOffsetModifier> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.codec("distance", e -> e.distance)
	).apply(i, ForwardOffsetModifier::new));

	public static ForwardOffsetModifier of(String str) {
		return new ForwardOffsetModifier(DoubleVariable.of(str));
	}

	@Override
	public ModifierType<ForwardOffsetModifier> type() {
		return LibraryRegistries.FORWARD.get();
	}

	@Override
	public LocationContext modify(EngineContext ctx) {
		return ctx.loc().add(ctx.loc().dir().scale(distance.eval(ctx)));
	}

}
