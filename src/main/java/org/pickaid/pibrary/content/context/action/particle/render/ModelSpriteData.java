package org.pickaid.pibrary.content.context.action.particle.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.pickaid.pibrary.content.context.action.particle.core.LMGenericParticle;
import net.minecraft.client.Camera;

public interface ModelSpriteData extends ParticleRenderer {

	@Override
	default void onPostTick(LMGenericParticle e) {

	}

	@Override
	default boolean specialRender(LMGenericParticle lmGenericParticle, VertexConsumer vc, Camera camera, float pTick) {
		return false;
	}

}
