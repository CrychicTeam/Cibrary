package org.pickaid.pibrary.content.context.action.engine.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.content.context.action.engine.core.Modifier;
import org.pickaid.pibrary.content.context.action.engine.core.ModifierType;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.init.LibraryObjects;
import net.minecraft.world.phys.Vec3;

public record SetPosModifier(DoubleVariable x, DoubleVariable y, DoubleVariable z)
		implements Modifier<SetPosModifier> {

	public static Codec<SetPosModifier> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.optionalCodec("x", SetPosModifier::x),
			DoubleVariable.optionalCodec("y", SetPosModifier::y),
			DoubleVariable.optionalCodec("z", SetPosModifier::z)
	).apply(i, (x, y, z) -> new SetPosModifier(
			x.orElse(DoubleVariable.ZERO),
			y.orElse(DoubleVariable.ZERO),
			z.orElse(DoubleVariable.ZERO))));

	@Override
	public ModifierType<SetPosModifier> type() {
		return LibraryObjects.POSITION.get();
	}

	@Override
	public LocationContext modify(EngineContext ctx) {
		return ctx.loc().with(new Vec3(
				x.eval(ctx), y.eval(ctx), z.eval(ctx)
		));
	}

}
