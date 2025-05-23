package org.pickaid.pibrary.content.context.action.engine.iterator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.content.context.variable.IntVariable;
import org.pickaid.pibrary.init.LibraryObjects;

import javax.annotation.Nullable;
import java.util.Optional;

public record LinearIterator(DoubleVariable alongDir, Vec3 offset, DoubleVariable alongOffset, IntVariable step,
							 boolean startFromOrigin, ConfiguredEngine<?> child, @Nullable String index)
		implements Iterator<LinearIterator> {

	public static Codec<LinearIterator> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.optionalCodec("alongDir", e -> e.alongDir),
			Vec3.CODEC.optionalFieldOf("offset").forGetter(e -> Optional.of(e.offset)),
			DoubleVariable.optionalCodec("alongOffset", e -> e.alongOffset),
			IntVariable.codec("step", e -> e.step),
			Codec.BOOL.optionalFieldOf("startFromOrigin").forGetter(e -> Optional.of(e.startFromOrigin)),
			ConfiguredEngine.codec("child", e -> e.child),
			Codec.STRING.optionalFieldOf("index").forGetter(e -> Optional.ofNullable(e.index))
	).apply(i, (a, b, c, d, e, f, g) -> new LinearIterator(a.orElse(DoubleVariable.ZERO), b.orElse(Vec3.ZERO),
			c.orElse(DoubleVariable.ZERO), d, e.orElse(true), f, g.orElse(null))));

	@Override
	public EngineType<LinearIterator> type() {
		return LibraryObjects.ITERATE_LINEAR.get();
	}

	@Override
	public void execute(EngineContext ctx) {
		double alongDir = alongDir().eval(ctx);
		double alongOffset = alongOffset().eval(ctx);
		int step = step().eval(ctx);
		for (int i = 0; i < step; i++) {
			int x = startFromOrigin ? i : i + 1;
			Vec3 pos = ctx.loc().pos()
					.add(ctx.loc().dir().scale(x * alongDir))
					.add(offset.scale(x * alongOffset));
			ctx.iterateOn(ctx.loc().with(pos), index, i, child);
		}
	}

}
