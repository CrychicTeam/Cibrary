package org.pickaid.pibrary.content.context.action.entity.core;

import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.UserContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.core.EntityProcessor;
import org.pickaid.pibrary.content.context.action.engine.helper.Scheduler;
import org.pickaid.pibrary.content.context.action.entity.renderer.ProjectileRenderer;
import org.pickaid.pibrary.api.fastprojectileapi.entity.ProjectileMovement;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectileData {

	private static final int SALT_TICK = 0x342ab3c1, SALT_MOVE = 0xa6258bd1,
			SALT_HIT = 0xb286c235, SALT_RENDER = 0x1134ba51;

	public static final Set<String> DEFAULT_PARAMS = Set.of("TickCount",
			"ProjectileX", "ProjectileY", "ProjectileZ");

	public ProjectileParams params;

	private ResourceLocation id;

	private boolean init;

	private ProjectileConfig config;

	public ProjectileData() {
	}

	public ProjectileData(ProjectileParams params, Holder<ProjectileConfig> config) {
		this.params = params;
		this.id = config.unwrapKey().get().location();
		this.config = config.get();
	}

	public void onInject() {
		init = false;
		config = null;
	}

	@Nullable
	public ProjectileConfig getConfig(Level level) {
		if (!init) {
			config = level.registryAccess().registryOrThrow(LibraryRegistries.PROJECTILE).get(id);
		}
		return config;
	}

	private Map<String, Double> allParams(Pirojectile self) {
		var ans = new LinkedHashMap<>(params.params());
		ans.put("TickCount", (double) self.tickCount);
		ans.put("ProjectileX", self.getX());
		ans.put("ProjectileY", self.getY() + self.getBbHeight() / 2);
		ans.put("ProjectileZ", self.getZ());
		return ans;
	}

	@Nullable
	private EngineContext getContext(Pirojectile self, int salt, boolean addScheduler) {
		if (!(self.getOwner() instanceof LivingEntity user)) return null;
		var source = new SingleThreadedRandomSource(params.seed() ^ self.tickCount ^ salt);
		Scheduler sche = addScheduler ? new Scheduler() : null;
		return new EngineContext(new UserContext(user.level(), user, sche),
				self.location(), source, allParams(self));
	}

	public boolean shouldHurt(Pirojectile self, Entity target) {
		if (getConfig(self.level()) == null) return false;
		if (self.getOwner() instanceof LivingEntity le) {
			return config.filter().test(target, le);
		}
		return false;
	}

	public void hurtTarget(Pirojectile self, EntityHitResult result) {
		if (getConfig(self.level()) == null) return;
		EntityProcessor<?> hit = config.hit();
		if (hit == null) return;
		if (!(result.getEntity() instanceof LivingEntity le)) return;
		EngineContext ctx = getContext(self, SALT_HIT, false);
		if (ctx == null) return;
		hit.process(List.of(le), ctx);
	}

	public void tick(Pirojectile self) {
		if (getConfig(self.level()) == null) return;
		ConfiguredEngine<?> tick = config.tick();
		if (tick == null) return;
		EngineContext ctx = getContext(self, SALT_TICK, true);
		if (ctx == null) return;
		tick.execute(ctx);
		ctx.registerScheduler();
	}

	public ProjectileMovement move(Pirojectile self, Vec3 vec, Vec3 pos) {
		if (getConfig(self.level()) != null) {
			Motion<?> motion = config.motion();
			EngineContext ctx = getContext(self, SALT_MOVE, false);
			if (motion != null && ctx != null) {
				return motion.move(ctx, vec, pos);
			}
		}
		return ProjectileMovement.of(vec);
	}

	@OnlyIn(Dist.CLIENT)
	@Nullable
	public ProjectileRenderer getRenderer(Pirojectile self) {
		if (getConfig(self.level()) == null) return null;
		var renderer = config.renderer();
		if (renderer == null) return null;
		EngineContext ctx = getContext(self, SALT_RENDER, false);
		if (ctx == null) return null;
		return renderer.resolve(ctx);
	}

}
