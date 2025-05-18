package org.pickaid.pibrary.content.context.action.entity.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.action.engine.helper.EffectInstanceEntry;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;

import java.util.List;
import java.util.Optional;

public record ArrowShoot(
		DoubleVariable speed,
		List<EffectInstanceEntry> effects
) implements AbstractArrowShoot<ArrowShoot> {

	public static final Codec<ArrowShoot> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.optionalCodec("speed", e -> e.speed),
			Codec.list(EffectInstanceEntry.CODEC).optionalFieldOf("effects").forGetter(e -> Optional.of(e.effects))
	).apply(i, (s, l) -> new ArrowShoot(s.orElse(DoubleVariable.of("3")), l.orElse(List.of()))));

	@Override
	public EngineType<ArrowShoot> type() {
		return LibraryRegistries.ARROW.get();
	}

	@Override
	public AbstractArrow arrow(EngineContext ctx) {
		var ans = new Arrow(ctx.user().level(), ctx.user().user());
		for (var e : effects) {
			ans.addEffect(e.get(ctx));
		}
		return ans;
	}

}
