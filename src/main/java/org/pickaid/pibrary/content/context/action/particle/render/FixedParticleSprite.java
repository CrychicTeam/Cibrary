package org.pickaid.pibrary.content.context.action.particle.render;

import org.pickaid.pibrary.content.context.action.particle.core.LMGenericParticle;
import org.pickaid.pibrary.content.context.action.particle.engine.RenderTypePreset;
import net.minecraft.resources.ResourceLocation;

public record FixedParticleSprite(
		RenderTypePreset renderType,
		ResourceLocation particle,
		int age, int total
) implements VanillaParticleSprite {

	@Override
	public void onParticleInit(LMGenericParticle e) {
		e.setSprite(spriteSet().get(age, total));
	}

	@Override
	public void onPostTick(LMGenericParticle e) {
	}

}
