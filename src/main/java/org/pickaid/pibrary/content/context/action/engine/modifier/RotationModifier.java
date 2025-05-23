package org.pickaid.pibrary.content.context.action.engine.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.content.context.action.engine.core.Modifier;
import org.pickaid.pibrary.content.context.action.engine.core.ModifierType;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.init.LibraryObjects;

public record RotationModifier(
		DoubleVariable degree,
		DoubleVariable vertical
) implements Modifier<RotationModifier> {

	public static Codec<RotationModifier> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.codec("degree", e -> e.degree),
			DoubleVariable.optionalCodec("vertical", e -> e.vertical)
	).apply(i, (a, b) -> new RotationModifier(a, b.orElse(DoubleVariable.ZERO))));

	public static RotationModifier of(String str) {
		return new RotationModifier(DoubleVariable.of(str), DoubleVariable.ZERO);
	}

	public static RotationModifier of(String str, String ver) {
		return new RotationModifier(DoubleVariable.of(str), DoubleVariable.of(ver));
	}

	@Override
	public ModifierType<RotationModifier> type() {
		return LibraryObjects.ROTATE.get();
	}

	@Override
	public LocationContext modify(EngineContext ctx) {
		return ctx.loc().rotateDegree(degree.eval(ctx), vertical.eval(ctx));
	}

}
