package org.pickaid.pibrary.content.context.action.particle.render;

import org.pickaid.pibrary.content.context.action.particle.core.LMGenericParticle;
import org.pickaid.pibrary.content.context.action.particle.engine.RenderTypePreset;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public record DustParticleSprite(RenderTypePreset renderType, Vector3f color) implements VanillaParticleSprite {

	private static final ResourceLocation ID = new ResourceLocation("dust");

	@Override
	public ResourceLocation particle() {
		return ID;
	}

	@Override
	public void onParticleInit(LMGenericParticle e) {
		VanillaParticleSprite.super.onParticleInit(e);
		e.setColor(color.x, color.y, color.z);
	}

}
