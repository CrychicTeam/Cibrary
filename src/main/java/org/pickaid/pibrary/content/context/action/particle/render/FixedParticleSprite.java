package org.pickaid.pibrary.content.context.action.particle.render;

import org.pickaid.pibrary.content.context.action.particle.core.PiGenericParticle;
import org.pickaid.pibrary.content.context.action.particle.engine.RenderTypePreset;
import net.minecraft.resources.ResourceLocation;

public record FixedParticleSprite(
		RenderTypePreset renderType,
		ResourceLocation particle,
		int age, int total
) implements VanillaParticleSprite {

	@Override
	public void onParticleInit(PiGenericParticle e) {
		e.setSprite(spriteSet().get(age, total));
	}

	@Override
	public void onPostTick(PiGenericParticle e) {
	}

}
