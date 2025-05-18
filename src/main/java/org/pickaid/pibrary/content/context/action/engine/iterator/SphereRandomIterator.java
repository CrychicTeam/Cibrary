package org.pickaid.pibrary.content.context.action.engine.iterator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.content.context.variable.IntVariable;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public record SphereRandomIterator(DoubleVariable radius, IntVariable count,
								   ConfiguredEngine<?> child, @Nullable String index)
		implements Iterator<SphereRandomIterator> {

	public static Codec<SphereRandomIterator> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.codec("radius", SphereRandomIterator::radius),
			IntVariable.codec("count", SphereRandomIterator::count),
			ConfiguredEngine.codec("child", Iterator::child),
			Codec.STRING.optionalFieldOf("index").forGetter(e -> Optional.ofNullable(e.index()))
	).apply(i, (radius, count, child, index) -> new SphereRandomIterator(
			radius, count, child, index.orElse(null))));

	@Override
	public EngineType<SphereRandomIterator> type() {
		return LibraryRegistries.RANDOM_SPHERE.get();
	}

	@Override
	public void execute(EngineContext ctx) {
		int count = count().eval(ctx);
		var rand = ctx.rand();
		var radius = radius().eval(ctx);
		for (int i = 0; i < count; i++) {
			Vec3 off = new Vec3(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian()).normalize();
			ctx.iterateOn(LocationContext.of(ctx.loc().pos().add(off.scale(radius)), off), index, i, child);
		}
	}

}
