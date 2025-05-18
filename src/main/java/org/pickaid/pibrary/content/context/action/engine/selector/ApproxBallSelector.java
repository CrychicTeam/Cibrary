package org.pickaid.pibrary.content.context.action.engine.selector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.EntitySelector;
import org.pickaid.pibrary.content.context.action.engine.core.SelectorType;
import org.pickaid.pibrary.content.context.action.engine.helper.CollisionHelper;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashSet;

public record ApproxBallSelector(
		DoubleVariable r
) implements EntitySelector<ApproxBallSelector> {

	public static final Codec<ApproxBallSelector> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.codec("r", ApproxBallSelector::r)
	).apply(i, ApproxBallSelector::new));

	@Override
	public SelectorType<ApproxBallSelector> type() {
		return LibraryRegistries.BALL.get();
	}

	public LinkedHashSet<LivingEntity> find(ServerLevel sl, EngineContext ctx, SelectionType type) {
		Vec3 pos = ctx.loc().pos();
		double r = r().eval(ctx);
		var aabb = AABB.ofSize(pos, r * 2, r * 2, r * 2);
		LinkedHashSet<LivingEntity> list = new LinkedHashSet<>();
		var boxes = CollisionHelper.ball(pos, r);
		for (var e : type.select(sl, ctx, aabb)) {
			if (e instanceof LivingEntity le) {
				var box = le.getBoundingBox();
				if (CollisionHelper.intersects(box, boxes))
					list.add(le);
			}
		}
		return list;
	}

}
