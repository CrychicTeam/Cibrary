package org.pickaid.pibrary.content.context.action.entity.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import org.pickaid.pibrary.content.context.action.engine.context.BuilderContext;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.action.entity.core.Pirojectile;
import org.pickaid.pibrary.content.context.action.entity.core.ProjectileConfig;
import org.pickaid.pibrary.content.context.action.entity.core.ProjectileData;
import org.pickaid.pibrary.content.context.action.entity.core.ProjectileParams;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.content.context.variable.IntVariable;
import org.pickaid.pibrary.init.LibraryObjects;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record CustomProjectileShoot(
		DoubleVariable speed,
		Holder<ProjectileConfig> config,
		IntVariable life,
		boolean bypassWall,
		boolean bypassEntity,
		Map<String, DoubleVariable> params
) implements ShootProjectileInstance<CustomProjectileShoot> {

	public static final Codec<CustomProjectileShoot> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.optionalCodec("speed", e -> e.speed),
			ProjectileConfig.HOLDER.fieldOf("config").forGetter(e -> e.config),
			IntVariable.codec("life", e -> e.life),
			Codec.BOOL.optionalFieldOf("bypassWall").forGetter(e -> Optional.of(e.bypassWall)),
			Codec.BOOL.optionalFieldOf("bypassEntity").forGetter(e -> Optional.of(e.bypassEntity)),
			Codec.unboundedMap(Codec.STRING, DoubleVariable.CODEC).optionalFieldOf("params")
					.forGetter(e -> Optional.of(e.params))
	).apply(i, (s, c, l, w, e, p) -> new CustomProjectileShoot(
			s.orElse(DoubleVariable.of("3")), c, l,
			w.orElse(false), w.orElse(false), p.orElse(Map.of())
	)));

	@Override
	public EngineType<CustomProjectileShoot> type() {
		return LibraryObjects.CUSTOM_SHOOT.get();
	}

	@Override
	public Entity create(EngineContext ctx) {
		var ans = new Pirojectile(LibraryObjects.GENERIC_PROJECTILE.get(), ctx.user().level());
		LinkedHashMap<String, Double> map = new LinkedHashMap<>();
		for (var e : params.entrySet()) {
			map.put(e.getKey(), e.getValue().eval(ctx));
		}
		ProjectileParams paramSet = new ProjectileParams(
				life.eval(ctx), bypassWall, bypassEntity,
				ctx.rand().nextLong(), map
		);
		ProjectileData data = new ProjectileData(paramSet, config);
		ans.setup(data, ctx.loc().pos(), ctx.loc().dir().scale(speed.eval(ctx)));
		return ans;
	}

	@Override
	public boolean verify(BuilderContext ctx) {
		boolean pass = true;
		for (var e : config.get().params()) {
			if (!params.containsKey(e)) {
				ctx.error("Missing key [" + e + "]");
				pass = false;
			}
		}
		return ShootProjectileInstance.super.verify(ctx) | pass;
	}

}
