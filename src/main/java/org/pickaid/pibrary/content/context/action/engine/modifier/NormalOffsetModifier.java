package org.pickaid.pibrary.content.context.action.engine.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.content.context.action.engine.core.Modifier;
import org.pickaid.pibrary.content.context.action.engine.core.ModifierType;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.init.LibraryRegistries;

public record NormalOffsetModifier(DoubleVariable distance) implements Modifier<NormalOffsetModifier> {

	public static Codec<NormalOffsetModifier> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.codec("distance", e -> e.distance)
	).apply(i, NormalOffsetModifier::new));

	public static NormalOffsetModifier of(String str) {
		return new NormalOffsetModifier(DoubleVariable.of(str));
	}

	@Override
	public ModifierType<NormalOffsetModifier> type() {
		return LibraryRegistries.NORMAL_OFFSET.get();
	}

	@Override
	public LocationContext modify(EngineContext ctx) {
		return ctx.loc().add(ctx.loc().normal().scale(distance.eval(ctx)));
	}

}
