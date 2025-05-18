package org.pickaid.pibrary.content.context.action.engine.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.content.context.action.engine.core.Modifier;
import org.pickaid.pibrary.content.context.action.engine.core.ModifierType;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.world.phys.Vec3;

public record OffsetModifier(DoubleVariable x, DoubleVariable y, DoubleVariable z)
		implements Modifier<OffsetModifier> {

	public static Codec<OffsetModifier> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.optionalCodec("x", OffsetModifier::x),
			DoubleVariable.optionalCodec("y", OffsetModifier::y),
			DoubleVariable.optionalCodec("z", OffsetModifier::z)
	).apply(i, (x, y, z) -> new OffsetModifier(
			x.orElse(DoubleVariable.ZERO),
			y.orElse(DoubleVariable.ZERO),
			z.orElse(DoubleVariable.ZERO))));

	public static OffsetModifier of(String x, String y, String z) {
		return new OffsetModifier(
				DoubleVariable.of(x),
				DoubleVariable.of(y),
				DoubleVariable.of(z));
	}

	@Override
	public ModifierType<OffsetModifier> type() {
		return LibraryRegistries.OFFSET.get();
	}

	@Override
	public LocationContext modify(EngineContext ctx) {
		return ctx.loc().add(new Vec3(
				x.eval(ctx), y.eval(ctx), z.eval(ctx)
		));
	}

}
