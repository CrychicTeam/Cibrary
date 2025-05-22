package org.pickaid.pibrary.content.context.action.particle.core;

import org.pickaid.pibrary.api.fastprojectileapi.entity.ProjectileMovement;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.entity.core.Motion;
import org.pickaid.pibrary.content.context.action.entity.motion.SimpleMotion;
import org.pickaid.pibrary.content.context.action.particle.engine.RenderTypePreset;
import org.pickaid.pibrary.content.context.action.particle.render.ParticleRenderer;
import org.pickaid.pibrary.content.context.action.particle.render.SimpleParticleSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ClientParticleData(
		int life, boolean doCollision, float size,
		EngineContext ctx, Motion<?> motion, ParticleRenderer renderer
) implements PiParticleData {

	public static final PiParticleData DEFAULT = new ClientParticleData(
			40, false, 0.15f, null, SimpleMotion.ZERO,
			new SimpleParticleSprite(
					RenderTypePreset.LIT,
					new ResourceLocation("flame")
			));

	public static float randSize(EngineContext ctx) {
		return 0.1F * (ctx.rand().nextFloat() * 0.5F + 0.5F) * 2.0F;
	}

	public ProjectileMovement move(int age, Vec3 velocity, Vec3 position) {
		return motion.move(ctx.withParam("TickCount", age), velocity, position);
	}

}
