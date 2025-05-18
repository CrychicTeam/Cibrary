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

public record SetNormalModifier(DoubleVariable x, DoubleVariable y, DoubleVariable z)
		implements Modifier<SetNormalModifier> {

	public static Codec<SetNormalModifier> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.optionalCodec("x", SetNormalModifier::x),
			DoubleVariable.optionalCodec("y", SetNormalModifier::y),
			DoubleVariable.optionalCodec("z", SetNormalModifier::z)
	).apply(i, (x, y, z) -> new SetNormalModifier(
			x.orElse(DoubleVariable.ZERO),
			y.orElse(DoubleVariable.ZERO),
			z.orElse(DoubleVariable.ZERO))));

	@Override
	public ModifierType<SetNormalModifier> type() {
		return LibraryRegistries.NORMAL.get();
	}

	@Override
	public LocationContext modify(EngineContext ctx) {
		return ctx.loc().setNormal(new Vec3(
				x.eval(ctx), y.eval(ctx), z.eval(ctx)
		).normalize());
	}

}
