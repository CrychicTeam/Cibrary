package org.pickaid.pibrary.content.context.action.particle.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.pickaid.pibrary.content.context.action.particle.core.PiGenericParticle;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.SpriteSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface SpriteData extends ParticleRenderer {

	@OnlyIn(Dist.CLIENT)
	SpriteSet spriteSet();

	@OnlyIn(Dist.CLIENT)
	@Override
	default void onParticleInit(PiGenericParticle e) {
		e.setSprite(spriteSet().get(0, 1));
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	default void onPostTick(PiGenericParticle e) {
		e.setSpriteFromAge(spriteSet());
	}

	@Override
	default boolean specialRender(PiGenericParticle e, VertexConsumer vc, Camera camera, float pTick) {
		return false;
	}

}
