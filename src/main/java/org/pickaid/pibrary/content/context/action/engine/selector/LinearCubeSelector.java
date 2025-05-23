package org.pickaid.pibrary.content.context.action.engine.selector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.EntitySelector;
import org.pickaid.pibrary.content.context.action.engine.core.SelectorType;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.content.context.variable.IntVariable;
import org.pickaid.pibrary.init.LibraryObjects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashSet;

public record LinearCubeSelector(
		IntVariable step,
		DoubleVariable size
) implements EntitySelector<LinearCubeSelector> {

	public static final Codec<LinearCubeSelector> CODEC = RecordCodecBuilder.create(i -> i.group(
			IntVariable.codec("step", LinearCubeSelector::step),
			DoubleVariable.codec("size", LinearCubeSelector::size)
	).apply(i, LinearCubeSelector::new));

	@Override
	public SelectorType<LinearCubeSelector> type() {
		return LibraryObjects.LINEAR.get();
	}

	public LinkedHashSet<LivingEntity> find(ServerLevel sl, EngineContext ctx, SelectionType type) {
		Vec3 pos = ctx.loc().pos();
		int step = step().eval(ctx);
		double diam = size().eval(ctx);
		LinkedHashSet<LivingEntity> list = new LinkedHashSet<>();
		for (int i = 0; i <= step; i++) {
			Vec3 p = pos.add(ctx.loc().dir().scale(i * diam));
			var aabb = AABB.ofSize(p, diam, diam, diam);
			for (var e : type.select(sl, ctx, aabb)) {
				if (e instanceof LivingEntity le) {
					list.add(le);
				}
			}
		}
		return list;
	}

}
