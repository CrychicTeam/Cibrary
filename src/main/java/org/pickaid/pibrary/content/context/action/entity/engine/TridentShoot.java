package org.pickaid.pibrary.content.context.action.entity.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Items;

public record TridentShoot(
		DoubleVariable speed
) implements AbstractArrowShoot<TridentShoot> {

	public static final Codec<TridentShoot> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.optionalCodec("speed", e -> e.speed)
	).apply(i, (s) -> new TridentShoot(s.orElse(DoubleVariable.of("3")))));

	@Override
	public EngineType<TridentShoot> type() {
		return LibraryRegistries.TRIDENT.get();
	}

	@Override
	public AbstractArrow arrow(EngineContext ctx) {
		return new ThrownTrident(ctx.user().level(), ctx.user().user(), Items.TRIDENT.getDefaultInstance());
	}

}
